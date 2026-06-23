package com.kc.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kc.system.component.OperationLogHelper;
import com.kc.system.entity.SysRole;
import com.kc.system.entity.SysUser;
import com.kc.system.service.SysRoleService;
import com.kc.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final OperationLogHelper operationLogHelper;

    private static final int PAGE_SIZE = 10;

    /**
     * 用户列表（分页）
     */
    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "") String keyword,
                       Model model) {
        IPage<SysUser> pageResult = sysUserService.pageByKeyword(
                new Page<>(page, PAGE_SIZE), keyword);
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        return "user/list";
    }

    /**
     * 新增用户页面
     */
    @GetMapping("/add")
    public String addPage(Model model) {
        model.addAttribute("user", new SysUser());
        return "user/add";
    }

    /**
     * 新增用户提交
     */
    @PostMapping("/add")
    public String addSubmit(@ModelAttribute SysUser user,
                            @RequestParam(value = "confirmPassword", defaultValue = "") String confirmPassword,
                            RedirectAttributes ra) {
        if (!StringUtils.hasText(user.getUsername())) {
            ra.addFlashAttribute("error", "用户名不能为空");
            return "redirect:/user/add";
        }
        if (!StringUtils.hasText(user.getPassword())) {
            ra.addFlashAttribute("error", "密码不能为空");
            return "redirect:/user/add";
        }
        if (!user.getPassword().equals(confirmPassword)) {
            ra.addFlashAttribute("error", "两次输入的密码不一致，请重新输入");
            return "redirect:/user/add";
        }
        if (sysUserService.existsByUsername(user.getUsername())) {
            ra.addFlashAttribute("error", "用户名已存在：" + user.getUsername());
            return "redirect:/user/add";
        }
        sysUserService.addUser(user);
        ra.addFlashAttribute("success", "用户添加成功");
        return "redirect:/user/list";
    }

    /**
     * 编辑用户页面
     */
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return "redirect:/user/list";
        }
        // 不回显密码
        user.setPassword("");
        model.addAttribute("user", user);
        return "user/edit";
    }

    /**
     * 编辑用户提交
     */
    @PostMapping("/edit")
    public String editSubmit(@ModelAttribute SysUser user, RedirectAttributes ra) {
        sysUserService.updateUser(user);
        ra.addFlashAttribute("success", "用户更新成功");
        return "redirect:/user/list";
    }

    /**
     * 切换用户状态（启用↔停用）
     */
    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails currentUser,
                         RedirectAttributes ra) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            ra.addFlashAttribute("error", "用户不存在");
            return "redirect:/user/list";
        }
        if (currentUser != null && currentUser.getUsername().equals(user.getUsername())) {
            ra.addFlashAttribute("error", "不能对当前登录账号进行停用操作");
            return "redirect:/user/list";
        }
        SysUser update = new SysUser();
        update.setId(id);
        update.setUsername(user.getUsername());
        update.setStatus(user.getStatus() == 1 ? 0 : 1);
        sysUserService.updateUser(update);
        String action = update.getStatus() == 1 ? "启用" : "停用";
        ra.addFlashAttribute("success", "[​" + user.getUsername() + "] 已" + action);
        return "redirect:/user/list";
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails currentUser,
                         RedirectAttributes ra) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            ra.addFlashAttribute("error", "用户不存在");
            return "redirect:/user/list";
        }
        // 不允许删除自己
        if (currentUser != null && currentUser.getUsername().equals(user.getUsername())) {
            ra.addFlashAttribute("error", "不能删除当前登录的账号");
            return "redirect:/user/list";
        }
        sysUserService.removeById(id);
        ra.addFlashAttribute("success", "用户删除成功");
        return "redirect:/user/list";
    }

    /**
     * 为用户分配角色（AJAX接口）
     */
    @PostMapping("/assign-role/{userId}")
    @ResponseBody
    public Map<String, Object> assignRole(@PathVariable Long userId,
                                           @RequestParam(required = false) List<Long> roleIds) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        try {
            sysRoleService.assignRoles(userId, roleIds);
            // 清除用户认证缓存
            SysUser user = sysUserService.getById(userId);
            if (user != null) {
                sysUserService.evictAuthCache(user.getUsername());
            }
            result.put("success", true);
            result.put("message", "角色分配成功");
            
            // 记录日志
            long costTime = System.currentTimeMillis() - startTime;
            operationLogHelper.logSuccess(
                "分配用户角色: " + (user != null ? user.getUsername() : userId),
                "POST /user/assign-role/" + userId,
                "roleIds=" + roleIds,
                costTime
            );
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "角色分配失败: " + e.getMessage());
            
            // 记录错误日志
            long costTime = System.currentTimeMillis() - startTime;
            operationLogHelper.logError(
                "分配用户角色失败",
                "POST /user/assign-role/" + userId,
                "roleIds=" + roleIds,
                e.getMessage(),
                costTime
            );
        }
        return result;
    }

    /**
     * 获取用户角色和所有角色列表（AJAX接口）
     */
    @GetMapping("/role-info/{userId}")
    @ResponseBody
    public Map<String, Object> getUserRoleInfo(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        // 获取所有启用的角色
        List<SysRole> allRoles = sysRoleService.listEnabledRoles();
        // 获取用户已分配的角色ID
        List<Long> userRoleIds = sysRoleService.getUserRoleIds(userId);
        
        result.put("allRoles", allRoles);
        result.put("userRoleIds", userRoleIds);
        return result;
    }
}
