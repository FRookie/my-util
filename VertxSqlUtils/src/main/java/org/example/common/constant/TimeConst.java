package org.example.common.constant;

/**
 * 时间常量
 * @date 2021-01-26
 */
public interface TimeConst {

    /**
     * 秒
     */
    interface Second {
        //10秒
        Integer SECOND_10 = 10;
        //1分钟
        Integer MINUTE_1 = 60;
        //1小时
        Integer HOUR_1 = 3600;
        //10分钟
        Integer MINUTE_10 = 600;
        //30分钟
        Integer MINUTE_30 = 1800;
        //5分钟
        Integer MINUTE_5 = 300;
        //一天
        Integer DAY_1 = 60*60*24;
        //七天
        Integer DAY_7 = 60*60*24*7;
        //2小时
        long HOUR_2 = 2*60*60;
        //不精确的6个月
        Integer DAY_180 = 60*60*24*180;
    }

    /**
     * 毫秒
     */
    interface Millis {
        //1秒
        Integer SECOND_1 = 1000;
        //1分钟
        Integer MINUTE_1 = 60000;
        //5分钟
        Integer MINUTE_5 = 300000;
        //8分钟
        Integer MINUTE_8 = 480000;
        //10分钟
        Integer MINUTE_10 = 600_000;
        //1小时
        Integer HOUR_1 = 60 * 60 * 1000;
        //2小时
        Integer HOUR_2 = 2 * 60 * 60 * 1000;
        //1天
        Integer DAY_1 = 24 * 60 * 60 * 1000;
        //30分钟
        Integer MINUTE_30 =  30 * 60 * 1000;
    }

}
