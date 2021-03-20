package com.second.kill.common.util;

import java.util.Date;

public final class DateUtils {


    /**
     * 提前几秒钟方法
     *
     * @param date   基准时间
     * @param second 需提前秒数
     * @return
     */
    public static Date advanceSecond(Date date, long second) {
        long resultDate = date.getTime() - second * 1000;
        return new Date(resultDate);
    }
}
