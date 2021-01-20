package com.second.kill.common.vo;

import lombok.Data;

import java.util.List;

@Data
public class ResultObjectVO<T> extends ResultVO {
    private Integer code;
    private String msg;
    private T data;

    public ResultObjectVO()
    {
        this(ResultVO.SUCCESS,"操作成功");
    }

    public ResultObjectVO(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
