package com.second.kill.common.persistence.service;



import com.second.kill.common.persistence.entity.EventProcess;

import java.util.List;

public interface EventProcessService {

    public List<EventProcess> queryList(EventProcess eventProcess);

    public int insert(EventProcess eventProcess);

    public int updateStatus(EventProcess eventProcess);

    public EventProcess findById(Long id);
}
