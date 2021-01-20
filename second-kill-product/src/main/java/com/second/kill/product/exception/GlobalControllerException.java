package com.second.kill.product.exception;

import com.second.kill.common.vo.ResultObjectVO;
import com.second.kill.common.vo.ResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalControllerException {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResultObjectVO handleException(Exception ex)
    {
        logger.warn(ex.getMessage(),ex);
        return new ResultObjectVO(ResultVO.FAILD,"操作异常");
    }
}
