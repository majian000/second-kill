package com.second.kill.common.persistence.entity;

import lombok.Data;

import java.util.Date;

/**
 * 发布事件表
 * @author majian
 */
@Data
public class EventPublish {
    private Long id; //主键
    private Short status; //消息状态 0:待发送 1:已发送
    private String payload; //消息内容
    private String type; //消息类型
    private String businessId; //业务主键
    private String transactionId; //事务ID,UUID
    private String remark; //备注
    private Date createDate; //创建时间

}
