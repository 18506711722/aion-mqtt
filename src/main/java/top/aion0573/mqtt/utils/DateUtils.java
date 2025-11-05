package top.aion0573.mqtt.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @包名 top.aion0573.utils
 * @名称 DateUtils
 * @描述 时间工具类
 * @创建者 AION
 * @创建时间 2018-05-23 14:20
 * @修改人 AION
 * @修改时间 2018-05-23 14:20
 * @版本 1.0
 **/
public class DateUtils {
    public static final String DATETIME_PATTERM = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERM = "yyyy-MM-dd";
    public static final String TIME_PATTERM = "HH:mm:ss";

    public static String format(Date date, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
    }


    public static Date parse(String date, String pattern) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.parse(date);
    }


    /**
     * 天转时间戳
     *
     * @param number
     * @return
     */
    public static long dayForTime(int number) {
        return number * 24 * 60 * 60 * 1000;
    }

    /**
     * 小时转时间戳
     *
     * @param number
     * @return
     */
    public static long hourForTime(int number) {
        return number * 60 * 60 * 1000;
    }

    /**
     * 分钟转时间戳
     *
     * @param number
     * @return
     */
    public static long minuteForTime(int number) {
        return number * 60 * 1000;
    }

    /**
     * 秒转时间戳
     *
     * @param number
     * @return
     */
    public static long secondForTime(int number) {
        return number * 1000;
    }


    public static int getNowYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }


    public static int getNowMonth() {
        return Calendar.getInstance().get(Calendar.MONTH) + 1;
    }

    /**
     * 获取当前时间月的第一天
     *
     * @return
     */
    public static Date getNowMonthFirstDay() {
        return getMonthFirstDay(new Date());
    }

    public static Date getNowMonthLastDay() {
        return getMonthLashDay(new Date());
    }

    /**
     * 获取上个月的第一天
     *
     * @return
     */
    public static Date getNowLastMonthFirstDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
        return getMonthFirstDay(calendar.getTime());
    }

    public static Date getNowLastMonthEndDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
        return getMonthLashDay(calendar.getTime());
    }

    /**
     * 获取时间月的第一天
     *
     * @param date
     * @return
     */
    public static Date getMonthFirstDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        int firstDay = calendar.getMinimum(Calendar.DATE);
        calendar.set(Calendar.DAY_OF_MONTH, firstDay);
        return calendar.getTime();
    }

    /**
     * 获取时间月的最后一天
     *
     * @param date
     * @return
     */
    public static Date getMonthLashDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int firstDay = calendar.getMaximum(Calendar.DATE);
        calendar.set(Calendar.DAY_OF_MONTH, firstDay);
        return calendar.getTime();
    }

    public static Date getWeekFirstDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK, 2);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getWeekLastDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Date weekFirstDay = getNowWeekFirstDay();
        calendar.setTime(weekFirstDay);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 6);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getNowWeekFirstDay() {
        return getWeekFirstDay(new Date());
    }

    public static Date getNowWeekLastDate() {
        return getWeekLastDay(new Date());
    }

    public static Date getNowLastWeekFirstDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getNowWeekFirstDay());
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - 7);
        return calendar.getTime();
    }

    public static Date getNowLastWeekLastDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getNowWeekLastDate());
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - 7);
        return calendar.getTime();
    }

    /**
     * 时间上增加几个月
     *
     * @param time
     * @param month
     * @return
     */
    public static Date addMonth(Date time, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        int oldMonth = calendar.get(Calendar.MONTH);
        calendar.set(Calendar.MONTH, oldMonth + month);
        return calendar.getTime();
    }


    public static Date addDay(Date time, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + day);
        return calendar.getTime();
    }

    public static Date getLastSecondOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    public static Date getFirstSecondOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getYesterday(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getNowYesterday() {
        return getYesterday(new Date());
    }

    /**
     * 获取俩个时间的小时差
     * @param startDate
     * @param endDate
     * @return
     */
    public static long getHourDifference(Date startDate, Date endDate) {
        return (endDate.getTime() - startDate.getTime()) / TimeUnit.HOURS.toMillis(1);
    }

    public static long getDayDifference(Date startDate, Date endDate){
        return (endDate.getTime() - startDate.getTime()) / TimeUnit.DAYS.toMillis(1);
    }

    public static Date getLastYearOfDay(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.YEAR,calendar.get(Calendar.YEAR)-1);
        return calendar.getTime();
    }

//    public static Date assembleDateTime(Date date, Time time){
//        date.setHours(time.getHours());
//        date.setMinutes(time.getMinutes());
//        date.setSeconds(time.getSeconds());
////        Calendar calendar = Calendar.getInstance();
////        calendar.set(date.getYear(),date.getMonth(),date.getDay(),time.getHours(),time.getMinutes(),time.getSeconds());
//        return date;
//    }

    public static Date assembleDateTime(Date date, Date time){
        date = (Date) date.clone();
        date.setHours(time.getHours());
        date.setMinutes(time.getMinutes());
        date.setSeconds(time.getSeconds());
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(date.getYear(),date.getMonth(),date.getDay(),time.getHours(),time.getMinutes(),time.getSeconds());
        return date;
    }

    public static int getDayOfWeek(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK)-1;
    }

    public static void main(String[] args) {
        System.out.println(format(getLastYearOfDay(new Date()), "yyyy-MM-dd HH:mm:ss"));
    }

}
