package com.kc.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kc.system.entity.SysApiCredential;
import com.kc.system.service.SysApiCredentialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 接口凭据管理
 */
@Controller
@RequestMapping("/credential")
@RequiredArgsConstructor
public class CredentialController {

    private final SysApiCredentialService credentialService;

    private static final int PAGE_SIZE = 10;

    /**
     * 凭据列表（分页）
     */
    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "") String keyword,
                       Model model) {
        IPage<SysApiCredential> pageResult = credentialService.pageByKeyword(
                new Page<>(page, PAGE_SIZE), keyword);
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        return "credential/list";
    }

    /**
     * 新增凭据页面
     */
    @GetMapping("/add")
    public String addPage(Model model) {
        model.addAttribute("credential", new SysApiCredential());
        return "credential/add";
    }

    /**
     * 新增凭据提交
     */
    @PostMapping("/add")
    public String addSubmit(@ModelAttribute SysApiCredential credential, RedirectAttributes ra) {
        if (!StringUtils.hasText(credential.getName())) {
            ra.addFlashAttribute("error", "凭据名称不能为空");
            return "redirect:/credential/add";
        }
        if (!StringUtils.hasText(credential.getAccessKey())) {
            ra.addFlashAttribute("error", "accessKey 不能为空");
            return "redirect:/credential/add";
        }
        if (!StringUtils.hasText(credential.getSecret())) {
            ra.addFlashAttribute("error", "secret 不能为空");
            return "redirect:/credential/add";
        }
        if (credentialService.existsByName(credential.getName(), null)) {
            ra.addFlashAttribute("error", "凭据名称已存在：" + credential.getName());
            return "redirect:/credential/add";
        }
        if (credentialService.existsByAccessKey(credential.getAccessKey(), null)) {
            ra.addFlashAttribute("error", "accessKey 已存在：" + credential.getAccessKey());
            return "redirect:/credential/add";
        }
        credential.setStatus(1);
        credentialService.save(credential);
        ra.addFlashAttribute("success", "凭据添加成功");
        return "redirect:/credential/list";
    }

    /**
     * 编辑凭据页面
     */
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        SysApiCredential credential = credentialService.getById(id);
        if (credential == null) {
            return "redirect:/credential/list";
        }
        model.addAttribute("credential", credential);
        return "credential/edit";
    }

    /**
     * 编辑凭据提交
     */
    @PostMapping("/edit")
    public String editSubmit(@ModelAttribute SysApiCredential credential, RedirectAttributes ra) {
        if (!StringUtils.hasText(credential.getName())) {
            ra.addFlashAttribute("error", "凭据名称不能为空");
            return "redirect:/credential/edit/" + credential.getId();
        }
        if (!StringUtils.hasText(credential.getAccessKey())) {
            ra.addFlashAttribute("error", "accessKey 不能为空");
            return "redirect:/credential/edit/" + credential.getId();
        }
        if (!StringUtils.hasText(credential.getSecret())) {
            ra.addFlashAttribute("error", "secret 不能为空");
            return "redirect:/credential/edit/" + credential.getId();
        }
        if (credentialService.existsByName(credential.getName(), credential.getId())) {
            ra.addFlashAttribute("error", "凭据名称已存在：" + credential.getName());
            return "redirect:/credential/edit/" + credential.getId();
        }
        if (credentialService.existsByAccessKey(credential.getAccessKey(), credential.getId())) {
            ra.addFlashAttribute("error", "accessKey 已存在：" + credential.getAccessKey());
            return "redirect:/credential/edit/" + credential.getId();
        }
        credentialService.updateById(credential);
        ra.addFlashAttribute("success", "凭据更新成功");
        return "redirect:/credential/list";
    }

    /**
     * 切换状态（启用↔禁用）
     */
    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        SysApiCredential credential = credentialService.getById(id);
        if (credential == null) {
            ra.addFlashAttribute("error", "凭据不存在");
            return "redirect:/credential/list";
        }
        SysApiCredential update = new SysApiCredential();
        update.setId(id);
        update.setStatus(credential.getStatus() == 1 ? 0 : 1);
        credentialService.updateById(update);
        String action = update.getStatus() == 1 ? "启用" : "禁用";
        ra.addFlashAttribute("success", "[" + credential.getName() + "] 已" + action);
        return "redirect:/credential/list";
    }

    /**
     * 删除凭据
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        SysApiCredential credential = credentialService.getById(id);
        if (credential == null) {
            ra.addFlashAttribute("error", "凭据不存在");
            return "redirect:/credential/list";
        }
        credentialService.removeById(id);
        ra.addFlashAttribute("success", "凭据删除成功");
        return "redirect:/credential/list";
    }
}
