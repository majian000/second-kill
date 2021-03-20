package com.second.kill.web.service.impl;

import com.second.kill.common.vo.ResultObjectVO;
import com.second.kill.common.vo.ResultVO;
import com.second.kill.web.service.SecondKillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SecondKillServiceImpl implements SecondKillService {


    @Autowired
    private StringRedisTemplate redisTemplate;



}
