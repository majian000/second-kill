package com.second.kill.common.persistence.service;


import com.second.kill.common.persistence.entity.MQTransaction;

import java.util.List;
import java.util.Map;

public interface MQTransactionService {

    public List<MQTransaction> queryList(MQTransaction mqTransaction);

    public int insert(MQTransaction mqTransaction);

}
