package org.dbm.common.util;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * 字符串处理辅助类
 * 请尽量先使用apache common lang下提供的函数
 */
public final class StringUtil {

    private final static Logger logger = LoggerFactory.getLogger(StringUtil.class);

    /**
     * 将指定对象转换为整数类型（多用于输入参数）
     * 为空时返回0
     */
    public static int convertToInt(Object objValue) {
        return convertToInt(objValue, 0);
    }
    public static Integer convertToInt(Object objValue, Integer defaultValue) {
        if (objValue == null) {
            return defaultValue;
        }
        if (objValue instanceof Number) {
            return ((Number) objValue).intValue();
        }
        String strValue = StringUtils.trimToNull(objValue.toString());
        if (strValue == null) {
            return defaultValue;
        }
        return NumberUtils.toInt(strValue);
    }

    /**
     * 将指定对象转换为长整数类型（多用于输入参数）
     * 为空时返回0
     */
    public static long convertToLong(Object objValue) {
        return convertToLong(objValue, 0L);
    }
    public static Long convertToLong(Object objValue, Long defaultValue) {
        if (objValue == null) {
            return defaultValue;
        }
        if (objValue instanceof Number) {
            return ((Number) objValue).longValue();
        }
        String strValue = StringUtils.trimToNull(objValue.toString());
        if (strValue == null) {
            return defaultValue;
        }
        return NumberUtils.toLong(strValue);
    }

    /**
     * 将指定对象转换为浮点数类型（多用于输入参数）
     * 为空时返回0
     */
    public static double convertToDouble(Object objValue) {
        if (objValue == null) {
            return 0;
        }
        if (objValue instanceof Number) {
            return ((Number) objValue).doubleValue();
        }
        String strValue = StringUtils.trimToNull(objValue.toString());
        if (strValue == null) {
            return 0;
        }
        return NumberUtils.toDouble(strValue);
    }

    /**
     * URLEncoder,默认UTF-8编码
     */
    public static String encode(String strValue) {
        try {
            return URLEncoder.encode(strValue, "UTF-8");
        } catch (Exception exp) {
            logger.error("URLEncoder编码转换出错", exp);
            return strValue;
        }
    }

    /**
     * URLDecoder,默认UTF-8编码
     */
    public static String decode(String strValue) {
        if (StringUtils.isBlank(strValue)) {
            return "";
        }
        try {
            return URLDecoder.decode(strValue, "UTF-8");
        } catch (Exception exp) {
            logger.error("URLDecoder编码转换出错", exp);
            return strValue;
        }
    }
}
