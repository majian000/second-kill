package com.second.kill.common.persistence.service;


import com.second.kill.common.persistence.entity.EventPublish;

import java.util.Date;
import java.util.List;

public interface EventPublishService {

    /**
     * 查询小于指定时间段的消息
     * @param date
     * @return
     */
    public List<EventPublish> queryFaildListByBefore(Date date);

    public List<EventPublish> queryList(EventPublish mqTransaction);

    public int insert(EventPublish mqTransaction);

    public int updateStatus(EventPublish eventPublish);

    public EventPublish findById(Long id);
}
