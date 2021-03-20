package com.second.kill.common.persistence.service.impl;

import com.second.kill.common.persistence.entity.EventPublish;
import com.second.kill.common.persistence.mapper.EventPublishMapper;
import com.second.kill.common.persistence.service.EventPublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class EventPublishServiceImpl implements EventPublishService {

    @Autowired
    private EventPublishMapper eventPublishMapper;

    @Override
    public List<EventPublish> queryFaildListByBefore(Date date) {
        return eventPublishMapper.queryFaildListByBefore(date);
    }

    @Override
    public List<EventPublish> queryList(EventPublish eventPublish) {
        return eventPublishMapper.queryList(eventPublish);
    }

    @Transactional
    @Override
    public int insert(EventPublish eventPublish) {
        return eventPublishMapper.insert(eventPublish);
    }

    @Override
    public int updateStatus(EventPublish eventPublish) {
        return eventPublishMapper.updateStatus(eventPublish);
    }

    @Override
    public EventPublish findById(Long id) {
        return eventPublishMapper.findById(id);
    }
}
