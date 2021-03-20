package com.second.kill.web.service;

import com.second.kill.common.vo.ResultVO;

import java.util.Map;

public interface SecondKillService {

    public ResultVO secondKill(String stockKey,Integer stock,Map<String, Object> queryMap);

}
