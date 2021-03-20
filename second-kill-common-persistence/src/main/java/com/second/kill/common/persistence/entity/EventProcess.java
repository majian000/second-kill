package com.second.kill.common.persistence.entity;

import lombok.Data;

import java.util.Date;

/**
 * 处理事件表
 * 每收到一次事件 就保存一次,如果期间执行失败 定时任务会轮训这张表 做事务补偿
 * @author majian
 */
@Data
public class EventProcess {
    private Long id; //主键
    private Short status; //消息状态 0:未处理 1:已处理
    private String payload; //消息内容
    private String type; //消息类型
    private String businessId; //业务主键
    private String transactionId; //事务ID,UUID
    private String tableName; //业务表名
    private String remark; //备注
    private Date createDate; //创建时间

}
