package org.example.common.util.date;


import org.example.common.constant.TimeConst;
import software.amazon.awssdk.utils.StringUtils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @date 2021/1/8 18:29.
 */
public class DateUtils {

    /**
     * 获取当前时间搓 秒
     */
    public static int getCurrentSec() {
        return Math.toIntExact(System.currentTimeMillis() / 1000);
    }

    /**
     * 获取当前时间搓 毫秒
     */
    public static long getCurrentMills() {
        return System.currentTimeMillis();
    }

    /**
     * 将字符串转换为时间戳
     *
     * @param dateString 时间格式：yyyy-mm-dd hh:mm:ss[.fffffffff]
     */
    public static long covertToMills(String dateString) {
        Timestamp timestamp = Timestamp.valueOf(dateString);
        return timestamp.getTime();
    }

    /**
     * 将时间字符串转换为时间搓
     *
     * @param dateString 时间格式：yyyy-mm-dd hh:mm:ss[.fffffffff]
     */
    public static int convertToSec(String dateString) {
        return Math.toIntExact(covertToMills(dateString) / 1000);
    }

    /**
     * 月份转时间搓,返回秒
     *
     * @param dateString 格式："yyyy-MM"
     */
    public static int convertMonthToSec(String dateString) {
        String dateTimeStr = dateString + "-01 00:00:00";
        return convertToSec(dateTimeStr);
    }

    /**
     * 月份转时间搓,返回 毫秒
     *
     * @param dateString 格式："yyyy-MM"
     */
    public static long convertMonthToMills(String dateString) {
        return convertMonthToSec(dateString) * 1000L;
    }

    /**
     * 将时间字符串转换为时间搓
     *
     * @param dateString 时间格式：yyyy-MM-dd HH:mm:ss
     */
    public static int convertDayToSec(String dateString) {
        dateString += " 00:00:00";
        return convertToSec(dateString);
    }

    /**
     * 时间戳转日期格式字符串 时间戳为0时返回空字符串
     *
     * @param seconds 秒数字符串
     * @return dateString 时间格式：yyyy-MM-dd HH:mm:ss
     */
    public static String timeStamp2Date(Integer seconds) {
        if (seconds == null) {
            return "";
        }
        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(seconds * 1000L));
    }

    /**
     * 时间戳转日期格式字符串 时间戳为0时返回空字符串
     *
     * @param seconds 秒数字符串
     * @return dateString 时间格式：yyyy-MM-dd HH:mm:ss
     */
    public static String timeStamp2Date(SimpleDateFormat sdf, Integer seconds) {
        if (seconds == null) {
            return "";
        }
        return sdf.format(new Date(seconds * 1000L));
    }

    /**
     * 毫秒转日期格式字符串 时间戳为0时返回空字符串
     *
     * @param mills 毫秒秒数字符串
     * @return dateString 时间格式：yyyy-MM-dd HH:mm:ss
     */
    public static String mills2Date(SimpleDateFormat sdf, Long mills) {
        if (mills == null) {
            return "";
        }
        return sdf.format(new Date(mills));
    }

    /**
     * 将秒数转换为日时分秒，
     *
     * @param seconds 秒数
     * @return >24小时：d天h小时m分s秒； <24小时：h小时m分s秒
     */
    public static String second2Time(String seconds) {
        if (StringUtils.isBlank(seconds) || "null".equalsIgnoreCase(seconds)) {
            return "";
        }
        Long second = Long.valueOf(seconds);
        long days = second / 86400;            //转换天数
        second = second % 86400;            //剩余秒数
        long hours = second / 3600;            //转换小时
        second = second % 3600;                //剩余秒数
        long minutes = second / 60;            //转换分钟
        second = second % 60;                //剩余秒数
        if (days > 0) {
            return days + "天" + hours + "小时" + minutes + "分" + second + "秒";
        } else {
            return hours + "小时" + minutes + "分" + second + "秒";
        }
    }

    /**
     * 获取指定某一天的开始时间戳
     * timeZone  如 GMT+8:00
     *
     * @param timeStamp 毫秒级时间戳
     */
    public static Long getDailyStartTime(Long timeStamp) {
        String timeZone = ZoneId.systemDefault().toString();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(timeZone));
        calendar.setTimeInMillis(timeStamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取当月开始时间戳
     * 如 GMT+8:00
     *
     * @param timeStamp 毫秒级时间戳
     */
    public static Long getMonthStartTime(Long timeStamp) {
        String timeZone = ZoneId.systemDefault().toString();
        // 获取当前日期
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(timeZone));
        calendar.setTimeInMillis(timeStamp);
        calendar.add(Calendar.YEAR, 0);
        calendar.add(Calendar.MONTH, 0);
        // 设置为1号,当前日期既为本月第一天
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取当月的结束时间戳
     * 如 GMT+8:00
     *
     * @param timeStamp 毫秒级时间戳
     */
    public static Long getMonthEndTime(Long timeStamp) {
        String timeZone = ZoneId.systemDefault().toString();
        Calendar calendar = Calendar.getInstance();// 获取当前日期
        calendar.setTimeZone(TimeZone.getTimeZone(timeZone));
        calendar.setTimeInMillis(timeStamp);
        calendar.add(Calendar.YEAR, 0);
        calendar.add(Calendar.MONTH, 0);
        // 获取当前月最后一天
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取指定时分的Calendar对象
     *
     * @param timeStr 时分字符串 HH:mm 例如08:00
     */
    public static Calendar getCalendar(String timeStr) {
        Calendar calendar = Calendar.getInstance();
        String[] arr = timeStr.trim().split(":");
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(arr[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(arr[1]));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }


    /**
     * 返回凌晨0点的时间搓 单位毫秒
     */
    public static long getZeroClockMills() {
        long currentMills = System.currentTimeMillis();
        long remain = currentMills % TimeConst.Millis.DAY_1;
        return currentMills - remain;
    }

    /**
     * 返回凌晨0点的时间搓 单位秒
     */
    public static int getZeroClockSec() {
        return Math.toIntExact(getZeroClockMills() / 1000);
    }

    /**
     * 返回jvm当前默认时区的偏移
     * 建议您始终通过指定所需/预期的时区来明确说明
     * 默认值可随时由JVM中任何线程中的任何代码更改.
     */
    public static ZoneOffset getDefaultZoneOffset() {
        OffsetDateTime odt = OffsetDateTime.now();
        ZoneOffset zoneOffset = odt.getOffset();
        return zoneOffset;
    }

    public static String getCurrentDateStr() {
        return timeStamp2Date(DateUtils.getCurrentSec());
    }

    public static boolean isHalfHour() {
        final int minute = getCurrentSec() / 60;
        return minute % 30 == 0;
    }
}
