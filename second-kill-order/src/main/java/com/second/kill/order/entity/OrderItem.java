package com.second.kill.order.entity;

import lombok.Data;

import java.util.Date;

/**
 * 订单子表
 *
 * @author majian
 */
@Data
public class OrderItem {
    private Long id; //主键
    private String orderNo; //订单编号
    private Long userId; //用户ID
    private Long orderId; //订单主表ID
    private Long skuId; //商品SKUID
    private Integer deliveryStatus; //配送状态 0未收货 1送货中 2已收货
    private Integer sellerStatus; //卖家备货状态 0备货中 1备货完成 2缺货
    private Integer buyerStatus; //买家状态 0待收货 1已收货 2换货 3退货
    private Integer productNum; //购买商品数量
    private Double productPrice; //商品单价
    private Double orderItemAmount; //订单单项总金额
    private Double deliveryMoney; //配送费用
    private Date deliveryReceiveTime; //收货时间
    private Date deliveryFinishTime; //配送人员完成时间
    private Date sellerFinishTime; //卖家完成时间
    private Date buyerFinishTime; //买家完成时间
    private String remark; //订单备注 买家填写
    private Date createDate; //创建时间

}
