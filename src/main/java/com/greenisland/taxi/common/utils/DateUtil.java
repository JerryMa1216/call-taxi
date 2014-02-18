package com.greenisland.taxi.common.utils;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {
	//一天的毫秒数 86400000 = 24*60*60*1000;
    private static final int millisPerDay = 86400000 ;
	 /** 计算系统日期与目标日期的相隔天数
     * @param tar 与系统时间对比的目标日期
     * @return 相隔天数, 参数无效返回-1
     * author: cxg 2009-5-24 下午02:16:54
     */
    public static int getIntervalDay(Date tar){
        int ret = -1;
        Calendar calNow = Calendar.getInstance();
        if(null != tar
                && tar.before(calNow.getTime())){//参数有效
 
            //获得指定时间的Calendar
            Calendar calTar = Calendar.getInstance();
            calTar.setTime(tar);
 
            long millisNow = calNow.getTimeInMillis();
            long millisTar = tar.getTime();
 
            //指定时间小于系统时间才处理， 否则返回空字符串
            if(millisTar < millisNow){
                //86400000 = 24*60*60*1000;
                ret = (int)((millisNow - millisTar) /(millisPerDay));
            }
        }
        return ret;
    }
}
