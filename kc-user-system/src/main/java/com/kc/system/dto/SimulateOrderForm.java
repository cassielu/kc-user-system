package com.kc.system.dto;

import lombok.Data;

@Data
public class SimulateOrderForm {

    /** 是否为导入模式（true=使用导入数据，不使用默认值） */
    private Boolean importMode = false;

    /** 接口凭据 ID（必填，关联 sys_api_credential） */
    private Long credentialId;

    /** 门店 ID */
    private String storeId = "10012";

    /** 外部订单号 */
    private String extOrderId;

    /** 外部订单编号（默认同 extOrderId） */
    private String extOrderNo;

    /** 第三方流水号 */
    private String thirdSn = "3";

    /** 经度 */
    private String longitude = "95.368757";

    /** 纬度 */
    private String latitude = "29.773897";

    /** 外部门店名称 */
    private String extStoreName = "t_9E8aG58c7k";

    /** 收件人姓名 */
    private String name = "陈先生";

    /** 收件人电话 */
    private String phone = "18466712239_9371";

    /** 配送地址 */
    private String address = "油炸小猫咪 (测试)";

    /** 用户备注 */
    private String userNote = "收餐人隐私号 18466712239_9371，手机号 178****0829 ";

    /** 订单类型 1-外卖 */
    private Integer orderType = 1;

    /** 是否预订单 1-是 */
    private Integer book = 1;

    /** 下单时间 */
    private String orderTime;

    /** 支付方式 2-在线支付 */
    private Integer payType = 2;

    /** 订单状态 7-已支付 */
    private Integer orderStatus = 7;

    /** 降级标记 */
    private Integer downgraded = 0;

    /** 是否已支付 */
    private Boolean isPayed = true;

    /** 第三方用户 ID */
    private String thirdUserId = "9382462186";

    /** 隐藏手机号 */
    private String hiddenPhone = "178****0829";

    /** 支付明细 JSON（数组格式字符串） */
    private String paymentDetailsJson = "[{\"type\":2,\"money\":12.11,\"typeName\":\"在线支付\"}]";

    /** 是否开发票 */
    private Boolean isInvoice = false;

    /** 就餐人数 */
    private Integer peopleNum = 0;

    /** 是否第三方配送 */
    private Boolean isThirdDistribute = false;

    /** 配送类型编码 */
    private String distributeTypeCode = "0000";

    /** 总价 */
    private Double price = 12.11;

    /** 配送费 */
    private Double deliveryFee = 0.01;

    /** 餐盒费 */
    private Double mealFee = 0.1;

    /** 折扣价 */
    private Double discountPrice = 0.0;

    /** 商家承担价格 */
    private Double merchantBearPrice = 0.0;

    /** 第三方平台承担价格 */
    private Double thirdPlatformBearPrice = 0.0;

    /** 商家价 */
    private Double merchantPrice = 11.63;

    /** 原价 */
    private Double originPrice = 0.0;

    /** 佣金 */
    private Double commission = 0.48;

    /** CPC 推广费 */
    private Double cpcAmount = 0.0;

    /** 加价 */
    private Double pricePremiums = 0.0;

    /** 对账扩展 JSON */
    private String reconciliationExtrasJson = "{\"chargeMode\":1,\"performanceServiceFee\":0.0,\"technicalServiceFee\":0.0,\"distanceFee\":0.0,\"priceFee\":0.0,\"slaFee\":0.0,\"baseShippingAmount\":0.0,\"categoryChargeFee\":0.0,\"weightChargeFee\":0.0,\"holidayChargeFee\":0.0}";

    /** FT 类型 JSON */
    private String ftTypeJson = "{\"emptyProductId\":true,\"productAberrant\":true,\"storeAberrant\":false}";

    /** 餐品列表 JSON */
    private String productsJson;

    /** 重量 */
    private Double weight = 0.0;

    /** 性别 0-未知 */
    private Integer sex = 0;

    /** 服务类型（mt/elm 等，用户自选） */
    private String serviceType = "mt";

    /** 供应商 */
    private String vendor = "";

    /** 请求地址（从凭据获取，可覆盖） */
    private String pushUrl;
}
