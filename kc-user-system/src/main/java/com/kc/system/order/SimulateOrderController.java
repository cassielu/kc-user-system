package com.kc.system.order;

import com.kc.system.config.OrderApiProperties;
import com.kc.system.dto.SimulateOrderForm;
import com.kc.system.entity.SysApiCredential;
import com.kc.system.service.SysApiCredentialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 模拟下单页面控制器
 */
@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class SimulateOrderController {

    private final SimulateOrderService simulateOrderService;
    private final OrderApiProperties orderApiProperties;
    private final SysApiCredentialService credentialService;

    @GetMapping("/simulate")
    public String simulatePage(Model model) {
        SimulateOrderForm form = new SimulateOrderForm();
        form.setExtOrderId(generateOrderId());
        // 预填默认餐品 JSON
        form.setProductsJson(simulateOrderService.buildDefaultProductsJson());
        // 预填下单时间
        form.setOrderTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        model.addAttribute("form", form);
        model.addAttribute("apiUrl", orderApiProperties.getUrl());
        model.addAttribute("credentials", credentialService.listActive());
        return "order/simulate";
    }

    @PostMapping("/simulate")
    public String submitOrder(@ModelAttribute SimulateOrderForm form, Model model) {
        String response = simulateOrderService.submitOrder(form);

        // 回填表单数据
        model.addAttribute("form", form);
        model.addAttribute("apiUrl", orderApiProperties.getUrl());
        model.addAttribute("credentials", credentialService.listActive());
        model.addAttribute("requestJson", simulateOrderService.buildRequestJson(form));
        model.addAttribute("response", response);
        return "order/simulate";
    }

    /** 生成唯一订单号：时间戳 + 6位随机数 */
    private String generateOrderId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = (int) (Math.random() * 900000) + 100000;
        return timestamp + random;
    }
}
