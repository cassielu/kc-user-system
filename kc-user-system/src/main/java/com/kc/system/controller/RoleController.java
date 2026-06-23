package com.kc.system.controller;

import com.kc.system.component.OperationLogHelper;
import com.kc.system.entity.SysRole;
import com.kc.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final SysRoleService sysRoleService;
    private final OperationLogHelper operationLogHelper;

    /**
     * 角色列表页面
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('menu:role:list')")
    public String listPage(Model model) {
        List<SysRole> roles = sysRoleService.listEnabledRoles();
        model.addAttribute("roles", roles);
        return "role/list";
    }

    /**
     * 权限分配页面
     */
    @GetMapping("/permission/{roleId}")
    @PreAuthorize("hasAuthority('menu:role:permission')")
    public String permissionPage(@PathVariable Long roleId, Model model) {
        // 获取角色信息
        SysRole role = sysRoleService.getById(roleId);
        model.addAttribute("role", role);

        // 获取所有菜单(树形)
        List<SysRoleService.MenuTreeNode> menuTree = sysRoleService.getMenuTree();

        // 获取该角色已分配的菜单ID
        List<Long> checkedMenuIds = sysRoleService.getRoleMenuIds(roleId);

        // 标记已选中的菜单
        menuTree.forEach(node -> {
            if (checkedMenuIds.contains(node.getId())) {
                node.setChecked(true);
            }
        });

        model.addAttribute("menuTree", menuTree);
        return "role/permission";
    }

    /**
     * 保存角色权限分配
     */
    @PostMapping("/permission/{roleId}")
    @ResponseBody
    @PreAuthorize("hasAuthority('menu:role:permission')")
    public Map<String, Object> savePermission(@PathVariable Long roleId,
                                               @RequestParam(required = false) List<Long> menuIds) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        try {
            sysRoleService.assignMenus(roleId, menuIds);
            result.put("success", true);
            result.put("message", "权限分配成功");
            
            // 记录日志
            long costTime = System.currentTimeMillis() - startTime;
            SysRole role = sysRoleService.getById(roleId);
            operationLogHelper.logSuccess(
                "分配角色权限: " + (role != null ? role.getRoleName() : roleId),
                "POST /role/permission/" + roleId,
                "menuIds=" + menuIds,
                costTime
            );
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "权限分配失败: " + e.getMessage());
            
            // 记录错误日志
            long costTime = System.currentTimeMillis() - startTime;
            operationLogHelper.logError(
                "分配角色权限失败",
                "POST /role/permission/" + roleId,
                "menuIds=" + menuIds,
                e.getMessage(),
                costTime
            );
        }
        return result;
    }

    /**
     * 新增角色页面
     */
    @GetMapping("/add")
    @PreAuthorize("hasAuthority('menu:role:add')")
    public String addPage() {
        return "role/add";
    }

    /**
     * 保存新增角色
     */
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('menu:role:add')")
    public String addRole(@ModelAttribute SysRole role) {
        sysRoleService.save(role);
        return "redirect:/role/list";
    }

    /**
     * 编辑角色页面
     */
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('menu:role:edit')")
    public String editPage(@PathVariable Long id, Model model) {
        SysRole role = sysRoleService.getById(id);
        model.addAttribute("role", role);
        return "role/edit";
    }

    /**
     * 保存编辑角色
     */
    @PostMapping("/edit")
    @PreAuthorize("hasAuthority('menu:role:edit')")
    public String editRole(@ModelAttribute SysRole role) {
        sysRoleService.updateById(role);
        return "redirect:/role/list";
    }

    /**
     * 删除角色
     */
    @PostMapping("/delete/{id}")
    @ResponseBody
    @PreAuthorize("hasAuthority('menu:role:delete')")
    public Map<String, Object> deleteRole(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            sysRoleService.removeById(id);
            result.put("success", true);
            result.put("message", "删除成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }
}
