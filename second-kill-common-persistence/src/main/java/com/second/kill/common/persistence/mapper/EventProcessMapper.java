package com.second.kill.common.persistence.mapper;

import com.second.kill.common.persistence.entity.EventProcess;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface EventProcessMapper {

    public List<EventProcess> queryList(EventProcess eventProcess);

    public int insert(EventProcess eventProcess);

    public EventProcess findById(Long id);

    public int updateStatus(EventProcess eventProcess);
}
