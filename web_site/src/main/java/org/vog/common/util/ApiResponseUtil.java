package org.vog.common.util;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.util.HashMap;
import java.util.Map;

/**
 * api返回值设置的处理辅助类
 * 参见接口文档，简介部分的关于接口返回值的说明
 */
public final class ApiResponseUtil {

    /**
     * 返回正常响应结果，没有业务具体返回数据（比如更新的场合）
     */
    public static Map<String, Object> success() {
        Map result = new HashMap();
        result.put("code", 0);
        return result;
    }

    /**
     * 返回正常响应结果，有业务具体返回数据
     */
    public static Map<String, Object> success(Map<String, Object> data) {
        Map result = new HashMap();
        result.put("code", 0);
        if (data != null) {
            result.put("data", data);
        }
        return result;
    }

    /**
     * 返回异常响应结果，包括error code和简短异常信息说明
     */
    public static Map<String, Object> error(int code, String msg, Object... args) {
        Map result = new HashMap();
        result.put("code", code);
        result.put("msg", arrayFormat(msg, args));
        return result;
    }

    /**
     * 返回异常响应结果，包括error code，简短异常信息说明和业务数据
     */
    public static Map<String, Object> errorWithData(int code, String msg, Map<String, Object> data, Object... args) {
        Map result = new HashMap();
        result.put("code", code);
        result.put("msg", arrayFormat(msg, args));
        if (data != null) {
            result.put("data", data);
        }
        return result;
    }

    // 这里直接引用slf4j的代码
    private static String arrayFormat(String messagePattern, Object[] argArray) {
        if (messagePattern == null) {
            return null;
        }
        FormattingTuple ft = MessageFormatter.arrayFormat(messagePattern, argArray);
        return ft.getMessage();
    }
}
