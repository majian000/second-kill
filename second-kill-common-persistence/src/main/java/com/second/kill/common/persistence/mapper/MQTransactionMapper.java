package com.second.kill.common.persistence.mapper;

import com.second.kill.common.persistence.entity.MQTransaction;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;


@Mapper
public interface MQTransactionMapper {

    public List<MQTransaction> queryList(MQTransaction mqTransaction);

    public int insert(MQTransaction mqTransaction);

}
