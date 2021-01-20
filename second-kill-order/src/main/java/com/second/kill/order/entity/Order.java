package com.second.kill.order.entity;

import lombok.Data;

import java.util.Date;

/**
 * 订单主表
 *
 * @author majian
 */
@Data
public class Order {
    private Long id; //主键
    private String orderNo; //订单编号
    private Long userId; //用户ID
    private Double orderAmount; //订单金额
    private Double payAmount; //付款金额
    private Double totalAmount; //商品最终金额(折扣算完)
    private Integer payStatus; //支付状态 0未支付 1已支付 2线下支付 3线下支付已到账 4取消支付
    private Integer tradeStatus; //交易状态 0进行中 1已完成 2已取消交易 3已结算
    private Integer payType; //交易类型 -1未确定 0微信 1支付宝
    private String outerTradeNo; //交易订单号(微信支付宝交易流水号)
    private Date bestDate; //最佳送货时间
    private Date payDate; //订单支付时间
    private String remark; //订单备注 买家填写
    private Date createDate; //创建时间

}
