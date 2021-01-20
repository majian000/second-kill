package com.second.kill.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class ResultListVO<T> extends ResultVO {
    private Integer code;
    private String msg;
    private List<T> data;

    public ResultListVO()
    {
        this(ResultVO.SUCCESS,"操作成功");
    }

    public ResultListVO(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
