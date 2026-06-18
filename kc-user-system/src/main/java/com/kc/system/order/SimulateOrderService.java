package com.kc.system.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kc.system.config.OrderApiProperties;
import com.kc.system.dto.SimulateOrderForm;
import com.kc.system.entity.SysApiCredential;
import com.kc.system.mapper.SysApiCredentialMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 模拟下单核心业务（负责：组装完整请求体、MD5签名、发送 POST、解析响应）
 *
 * 新请求结构：
 * {
 *   "accessKey": "...",
 *   "storeId": "...",
 *   "actionName": "candao.order.pushOrder",
 *   "data": { ... 订单数据 ... },
 *   "timestamp": 1781688165724,
 *   "sign": "md5值",
 *   "serviceType": "mt",
 *   "vendor": ""
 * }
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimulateOrderService {

    private final RestTemplate restTemplate;
    private final OrderApiProperties orderApiProperties;
    private final SysApiCredentialMapper credentialMapper;
    private final ObjectMapper objectMapper;

    /**
     * 提交订单：返回接口原始响应体
     */
    public String submitOrder(SimulateOrderForm form) {
        // 1. 查询接口凭据
        SysApiCredential credential = credentialMapper.selectById(form.getCredentialId());
        if (credential == null) {
            return "{\"success\":false,\"msg\":\"接口凭据不存在，请先在数据库中配置\"}";
        }
        if (credential.getStatus() != 1) {
            return "{\"success\":false,\"msg\":\"接口凭据已禁用\"}";
        }

        // 2. 组装完整请求体（含签名）
        Map<String, Object> requestBody = buildRequestBody(form, credential);
        // 提取 data 对象并序列化为紧凑 JSON（用于签名）
        Map<String, Object> dataContent = (Map<String, Object>) requestBody.get("data");
        String dataJson = toJson(dataContent);

        // 3. 计算签名
        long timestamp = System.currentTimeMillis();
        String sign = computeSign(credential.getAccessKey(), "candao.order.pushOrder", credential.getSecret(), timestamp, dataJson);
        requestBody.put("timestamp", timestamp);
        requestBody.put("sign", sign);

        // 4. 发送 HTTP POST（优先使用凭据的 pushUrl，fallback 到配置）
        String apiUrl = (credential.getPushUrl() != null && !credential.getPushUrl().isEmpty())
                ? credential.getPushUrl()
                : orderApiProperties.getUrl();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(toJson(requestBody), headers);

        log.info("[SimulateOrder] 请求地址: {}", apiUrl);
        log.debug("[SimulateOrder] 请求体: {}", toJson(requestBody));

        try {
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("[SimulateOrder] 接口调用异常: {}", e.getMessage());
            return "{\"success\":false,\"msg\":\"调用下单接口异常: " + e.getMessage() + "\"}";
        }
    }

    /**
     * 获取格式化的请求 JSON（用于页面预览，不含签名）
     */
    public String buildRequestJson(SimulateOrderForm form) {
        SysApiCredential credential = credentialMapper.selectById(form.getCredentialId());
        if (credential == null) {
            return "{\"error\":\"请选择接口凭据\"}";
        }
        Map<String, Object> requestBody = buildRequestBody(form, credential);
        Map<String, Object> dataContent = (Map<String, Object>) requestBody.get("data");
        String dataJson = toJson(dataContent);
        long timestamp = System.currentTimeMillis();
        String sign = computeSign(credential.getAccessKey(), "candao.order.pushOrder", credential.getSecret(), timestamp, dataJson);
        requestBody.put("timestamp", timestamp);
        requestBody.put("sign", sign);
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    /**
     * 返回默认餐品列表的格式化 JSON（供表单初始化预填）
     */
    public String buildDefaultProductsJson() {
        try {
            return objectMapper.writeValueAsString(buildDefaultProducts());
        } catch (Exception e) {
            return "[]";
        }
    }

    /** 组装顶层请求体 */
    @SuppressWarnings("unchecked")
    private Map<String, Object> buildRequestBody(SimulateOrderForm form, SysApiCredential credential) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("accessKey", credential.getAccessKey());
        root.put("storeId", form.getStoreId());
        root.put("actionName", "candao.order.pushOrder");
        root.put("data", buildDataContent(form));
        root.put("serviceType", form.getServiceType());
        root.put("vendor", form.getVendor());
        return root;
    }

    /** 构建 data 内部订单数据 */
    @SuppressWarnings("unchecked")
    private Map<String, Object> buildDataContent(SimulateOrderForm form) {
        Map<String, Object> data = new LinkedHashMap<>();
        boolean importMode = Boolean.TRUE.equals(form.getImportMode());

        data.put("extOrderId",      form.getExtOrderId());
        data.put("extOrderNo",      form.getExtOrderId());
        putIfNotImport(data, "thirdSn", form.getThirdSn(), importMode);
        putIfNotImport(data, "longitude", form.getLongitude(), importMode);
        putIfNotImport(data, "latitude", form.getLatitude(), importMode);
        data.put("storeId",         form.getStoreId());
        putIfNotImport(data, "extStoreName", form.getExtStoreName(), importMode);
        putIfNotImport(data, "name", form.getName(), importMode);
        putIfNotImport(data, "phone", form.getPhone(), importMode);
        putIfNotImport(data, "address", form.getAddress(), importMode);
        putIfNotImport(data, "userNote", form.getUserNote(), importMode);
        putIfNotImport(data, "orderType", form.getOrderType(), importMode);
        putIfNotImport(data, "book", form.getBook(), importMode);
        data.put("orderTime",       form.getOrderTime());
        putIfNotImport(data, "payType", form.getPayType(), importMode);
        putIfNotImport(data, "orderStatus", form.getOrderStatus(), importMode);
        putIfNotImport(data, "downgraded", form.getDowngraded(), importMode);
        putIfNotImport(data, "isPayed", form.getIsPayed(), importMode);
        putIfNotImport(data, "thirdUserId", form.getThirdUserId(), importMode);
        putIfNotImport(data, "hiddenPhone", form.getHiddenPhone(), importMode);

        // paymentDetails
        putIfNotImport(data, "paymentDetails", parseJson(form.getPaymentDetailsJson(), List.class), importMode);

        putIfNotImport(data, "isInvoice", form.getIsInvoice(), importMode);
        putIfNotImport(data, "peopleNum", form.getPeopleNum(), importMode);
        putIfNotImport(data, "isThirdDistribute", form.getIsThirdDistribute(), importMode);
        putIfNotImport(data, "distributeTypeCode", form.getDistributeTypeCode(), importMode);
        putIfNotImport(data, "price", form.getPrice(), importMode);
        putIfNotImport(data, "deliveryFee", form.getDeliveryFee(), importMode);
        putIfNotImport(data, "mealFee", form.getMealFee(), importMode);
        putIfNotImport(data, "discountPrice", form.getDiscountPrice(), importMode);
        putIfNotImport(data, "merchantBearPrice", form.getMerchantBearPrice(), importMode);
        putIfNotImport(data, "thirdPlatformBearPrice", form.getThirdPlatformBearPrice(), importMode);
        putIfNotImport(data, "merchantPrice", form.getMerchantPrice(), importMode);
        putIfNotImport(data, "originPrice", form.getOriginPrice(), importMode);
        putIfNotImport(data, "commission", form.getCommission(), importMode);
        putIfNotImport(data, "cpcAmount", form.getCpcAmount(), importMode);
        putIfNotImport(data, "pricePremiums", form.getPricePremiums(), importMode);

        // reconciliationExtras
        putIfNotImport(data, "reconciliationExtras", parseJson(form.getReconciliationExtrasJson(), Map.class), importMode);

        // ftType
        putIfNotImport(data, "ftType", parseJson(form.getFtTypeJson(), Map.class), importMode);

        // products
        data.put("products", resolveProducts(form.getProductsJson()));

        putIfNotImport(data, "weight", form.getWeight(), importMode);
        putIfNotImport(data, "allowanceServiceFee", 0.0, importMode);
        putIfNotImport(data, "contributionAmount", 0.0, importMode);
        putIfNotImport(data, "publicWelfareGoodsFee", 0.0, importMode);
        putIfNotImport(data, "isFavorites", true, importMode);
        putIfNotImport(data, "baseLogisticsServiceFee", 0.0, importMode);
        putIfNotImport(data, "distanceIncreaseFee", 0.0, importMode);
        putIfNotImport(data, "timeIntervalMarkUpFee", 0.0, importMode);
        putIfNotImport(data, "agentBearPrice", 0.0, importMode);
        putIfNotImport(data, "sex", form.getSex(), importMode);
        putIfNotImport(data, "surCharge", 0.0, importMode);
        putIfNotImport(data, "fulfillServiceFee", 0.0, importMode);
        putIfNotImport(data, "commissionReturnFee", 0.0, importMode);
        putIfNotImport(data, "isAbnormalOrder", false, importMode);
        putIfNotImport(data, "userPrepaidAmount", 0.0, importMode);
        putIfNotImport(data, "isSqtOrder", false, importMode);
        putIfNotImport(data, "isSqtInvoice", false, importMode);
        putIfNotImport(data, "lowincomeOrder", 0, importMode);
        putIfNotImport(data, "isOneUrgentDelivery", false, importMode);

        return data;
    }

    /**
     * 非导入模式下才 put 值（导入模式下由用户 JSON 提供，不使用默认值）
     */
    private void putIfNotImport(Map<String, Object> map, String key, Object value, boolean importMode) {
        if (!importMode || value != null) {
            map.put(key, value);
        }
    }

    /** 32位 MD5 签名：accessKey + actionName + secret + timestamp + dataJson */
    private String computeSign(String accessKey, String actionName, String secret, long timestamp, String dataJson) {
        StringBuilder sb = new StringBuilder();
        sb.append(accessKey);
        sb.append(actionName);
        sb.append(secret);
        sb.append(timestamp);
        if (dataJson != null && !dataJson.isEmpty()) {
            sb.append(dataJson);
        }
        return md5(sb.toString());
    }

    /** 32位小写 MD5 */
    private String md5(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5计算失败", e);
        }
    }

    /** 解析用户输入的餐品 JSON；内容为空或解析失败则返回默认餐品 */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> resolveProducts(String productsJson) {
        if (productsJson != null && !productsJson.trim().isEmpty()) {
            try {
                return objectMapper.readValue(productsJson, List.class);
            } catch (Exception e) {
                log.warn("[SimulateOrder] 餐品 JSON 解析失败，使用默认餐品: {}", e.getMessage());
            }
        }
        return buildDefaultProducts();
    }

    private List<Map<String, Object>> buildDefaultProducts() {
        // 默认带一个测试商品
        Map<String, Object> product = new LinkedHashMap<>();
        product.put("pid",            "1605680");
        product.put("subPid",         "1605680");
        product.put("platPid",        "20257458629");
        product.put("isDiscount",     false);
        product.put("name",           "mtddzx咖啡A");
        product.put("nameTranslation", Collections.emptyMap());
        product.put("price",          0.0);
        product.put("singleIncreasePrice", 0.0);
        product.put("totalPrice",     0.0);
        product.put("realTimeTotalPrice", 0.0);
        product.put("addPrice",       0.0);
        product.put("num",            1.0);
        product.put("boxNum",         0.0);
        product.put("boxPrice",       0.0);
        product.put("boxTotalNum",    0);
        product.put("boxTotalPrice",  0.0);
        product.put("productTagUid",  0);
        product.put("bagNo",          "1");
        product.put("groupNameTranslation", Collections.emptyMap());

        // SKUs
        List<Map<String, Object>> skus = new ArrayList<>();
        skus.add(buildSku("2499324", "[spu]1605680-2499324", "42449374921", 12.0, "迷你杯", false, null));
        skus.add(buildSkuOption("椰果加料", "42450375224", "{\"spuId\":\"2499379\"}", 0.0, "椰果b", 1));
        skus.add(buildSkuOption("咖啡加料", "42450375238", "{\"spuId\":\"2499385\"}", 0.0, "双份奶", 1));
        product.put("skus", skus);

        // propertys
        List<Map<String, Object>> propertys = new ArrayList<>();
        propertys.add(buildProperty("少冰"));
        propertys.add(buildProperty("微甜"));
        product.put("propertys", propertys);

        product.put("isMatchDisProduct", false);
        product.put("realTimePrice", 0.0);
        product.put("uniqueId", "7849138873620");
        product.put("extra", "{\"spuId\":\"20257458629\",\"skuId\":\"42449374921\"}");

        return Collections.singletonList(product);
    }

    private Map<String, Object> buildSku(String skuId, String subSkuId, String platSkuId,
                                          double price, String name, boolean isOption, String extra) {
        Map<String, Object> sku = new LinkedHashMap<>();
        sku.put("titleTranslation", Collections.emptyMap());
        sku.put("skuId", skuId);
        sku.put("subSkuId", subSkuId);
        sku.put("platSkuId", platSkuId);
        sku.put("price", price);
        sku.put("name", name);
        sku.put("nameTranslation", Collections.emptyMap());
        sku.put("isOption", isOption);
        if (extra != null) sku.put("extra", extra);
        return sku;
    }

    private Map<String, Object> buildSkuOption(String title, String platSkuId, String extra,
                                                double price, String name, int num) {
        Map<String, Object> sku = new LinkedHashMap<>();
        sku.put("title", title);
        sku.put("titleTranslation", Collections.emptyMap());
        sku.put("platSkuId", platSkuId);
        sku.put("extra", extra);
        sku.put("price", price);
        sku.put("name", name);
        sku.put("nameTranslation", Collections.emptyMap());
        sku.put("num", num);
        sku.put("isOption", true);
        return sku;
    }

    private Map<String, Object> buildProperty(String name) {
        Map<String, Object> prop = new LinkedHashMap<>();
        prop.put("titleTranslation", Collections.emptyMap());
        prop.put("name", name);
        prop.put("nameTranslation", Collections.emptyMap());
        return prop;
    }

    /** 安全解析 JSON，失败返回 null */
    @SuppressWarnings("unchecked")
    private <T> T parseJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) return null;
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.warn("[SimulateOrder] JSON 解析失败: {}", json);
            return null;
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
