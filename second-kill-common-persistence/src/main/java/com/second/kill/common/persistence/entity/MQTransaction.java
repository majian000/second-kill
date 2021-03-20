package com.second.kill.common.persistence.entity;

import lombok.Data;

import java.util.Date;

/**
 * MQ 用于回查本地事务
 * @author majian
 */
@Data
public class MQTransaction {
    private Long id; //主键
    private String businessId; //业务主键
    private String transactionId; //事务ID,UUID
    private String tableName; //业务表名
    private String remark; //备注
    private Date createDate; //创建时间

}
