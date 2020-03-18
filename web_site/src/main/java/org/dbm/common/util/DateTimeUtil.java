package org.dbm.common.util;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * 日期时间工具类 <br>
 * 提供一些常用的日期时间操作方法，所有方法都为静态，不用实例化该类即可使用。 <br>
 *
 */
public final class DateTimeUtil {

    private final static Logger logger = LoggerFactory.getLogger(DateTimeUtil.class);

    /**
     * 缺省的日期显示格式： yyyy-MM-dd
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 缺省的日期时间显示格式：yyyy-MM-dd HH:mm:ss
     */
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATETIME_FORMAT_YMDHM = "yyyy-MM-dd HH:mm";

    public static final String COMPRESS_MONTHDAY_FORMAT = "MMdd";
    public static final String COMPRESS_DATE_FORMAT = "yyyyMMdd";
    public static final String COMPRESS_DATETIME_FORMAT = "yyyyMMddHHmmssSSS";

    private static final TimeZone beijingZone = TimeZone.getTimeZone("Asia/Shanghai");

    /**
     * 私有构造方法，禁止对该类进行实例化
     */
    private DateTimeUtil() {
    }

    /**
     * 返回一个ThreadLocal的SimpleDateFormat,每个线程只会创建一次
     */
    private static FastDateFormat getDateFormat(final String pattern) {
        return FastDateFormat.getInstance(pattern, beijingZone); // 这里不需要缓存,FastDateFormat已内置
    }

    /**
     * 得到系统当前日期时间
     *
     * @return 当前日期时间
     */
    public static Date getDate() {
        return getCalendar().getTime();
    }

    /**
     * 得到系统当前Calendar对象
     *
     * @return 当前日期时间
     */
    public static Calendar getCalendar() {
        return Calendar.getInstance(beijingZone);
    }

    /**
     * 得到用缺省方式格式化的当前日期及时间
     *
     * @return 当前日期及时间
     */
    public static String getNow() {
        return getNow(DEFAULT_DATETIME_FORMAT);
    }

    /**
     * 得到系统当前日期及时间，并用指定的方式格式化
     *
     * @param pattern 显示格式
     * @return 当前日期及时间
     */
    public static String getNow(String pattern) {
        return format(getDate(), pattern);
    }

    /**
     * 得到系统当前日期(北京时间:yyyyMMddHHmmssSSS)
     */
    public static long getNowTime() {
        String date = DateTimeUtil.getNow(COMPRESS_DATETIME_FORMAT);
        return NumberUtils.toLong(date);
    }

    /**
     * 得到用指定方式格式化的日期
     *
     * @param date    需要进行格式化的日期
     * @param pattern 显示格式
     * @return 日期时间字符串
     */
    public static String format(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        String patternTmp = pattern;
        if (null == patternTmp || "".equals(patternTmp)) {
            patternTmp = DEFAULT_DATETIME_FORMAT;
        }

        return getDateFormat(patternTmp).format(date);
    }

    /**
     * 将一个字符串用给定的格式转换为字符串类型。 <br>
     * 注意：如果返回null，则表示解析失败
     *
     * @param dateSting 需要解析的日期字符串
     * @param pattern   日期字符串的格式，默认为“yyyy-MM-dd”的形式
     * @return 解析后的日期
     */
    public static String parseStr(String dateSting, String pattern) {
        String dateStingTmp = dateSting;
        String patternTmp = pattern;
        if (dateStingTmp == null || dateStingTmp.trim().length() == 0) {
            return null;
        }

        if (null == patternTmp || "".equals(patternTmp)) {
            patternTmp = DEFAULT_DATE_FORMAT;
        }

        return getDateFormat(patternTmp).format(parse(dateStingTmp, DEFAULT_DATETIME_FORMAT));
    }

    /**
     * 将一个字符串用默认为“yyyy-MM-dd HH:mm:ss”的形式转换为日期类型。 <br>
     * 注意：如果返回null，则表示解析失败
     *
     * @param dateStr 需要解析的日期字符串
     * @return 解析后的日期
     */
    public static Date parse(String dateStr) {
        return parse(dateStr, DEFAULT_DATETIME_FORMAT);
    }

    /**
     * 将一个字符串用给定的格式转换为日期类型。 <br>
     * 注意：如果返回null，则表示解析失败
     *
     * @param dateStr 需要解析的日期字符串
     * @param pattern 日期字符串的格式
     * @return 解析后的日期
     */
    public static Date parse(String dateStr, String pattern) {
        Date date = null;

        if (dateStr == null || dateStr.trim().length() == 0) {
            return null;
        }

        try {
            date = getDateFormat(pattern).parse(dateStr);
        } catch (ParseException e) {
            logger.debug("日期转换错误", e);
        }

        return date;
    }

}