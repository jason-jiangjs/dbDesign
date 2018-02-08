package org.vog.common;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取系统配置信息，可以动态刷新
 */
public class SystemProperty {

    private static Map<String, String> propertyMap = new HashMap<>();

    public static void initComConfig(List<Map> propList) {
        if (propList == null || propList.isEmpty()) {
            return;
        }
        if (propertyMap.size() > 0) {
            propertyMap.clear();
        }
        propList.forEach(item -> {
            propertyMap.put((String) item.get("propName"), (String) item.get("propValue"));
        });
    }

    public static String resolveSystemProperty(String name) {
        return propertyMap.get(name);
    }
    
    public static String resolveSystemProperty(String name, String defaultValue) {
        String rst = propertyMap.get(name);
        if (rst == null || rst.trim().length() == 0) {
            return defaultValue;
        }
        return rst;
    }


    public static int resolveIntSystemProperty(String name) {
        return NumberUtils.toInt(propertyMap.get(name));
    }
    
    public static int resolveIntSystemProperty(String name, int defaultValue) {
        return NumberUtils.toInt(propertyMap.get(name), defaultValue);
    }

    public static boolean resolveBooleanSystemProperty(String name) {
        return BooleanUtils.toBoolean(propertyMap.get(name));
    }
    
    public static boolean resolveBooleanSystemProperty(String name, boolean defaultValue) {
        return BooleanUtils.toBoolean(propertyMap.get(name));
    }


    public static long resolveLongSystemProperty(String name) {
        return NumberUtils.toLong(propertyMap.get(name));
    }
    
    public static long resolveLongSystemProperty(String name, long defaultValue) {
        return NumberUtils.toLong(propertyMap.get(name), defaultValue);
    }

}
