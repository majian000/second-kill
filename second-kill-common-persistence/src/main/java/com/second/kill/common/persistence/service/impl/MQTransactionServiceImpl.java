package com.second.kill.common.persistence.service.impl;

import com.second.kill.common.persistence.entity.MQTransaction;
import com.second.kill.common.persistence.mapper.MQTransactionMapper;
import com.second.kill.common.persistence.service.MQTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class MQTransactionServiceImpl implements MQTransactionService {

    @Autowired
    private MQTransactionMapper mqTransactionMapper;

    @Override
    public List<MQTransaction> queryList(MQTransaction mqTransaction) {
        return mqTransactionMapper.queryList(mqTransaction);
    }

    @Transactional
    @Override
    public int insert(MQTransaction mqTransaction) {
        return mqTransactionMapper.insert(mqTransaction);
    }
}
