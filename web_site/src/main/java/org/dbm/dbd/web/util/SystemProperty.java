package org.dbm.dbd.web.util;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.dbm.dbd.dao.ComConfigDao;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取系统配置信息，可以动态刷新
 * 开发环境时优先从配置文件取值
 * 时优正式环境先从数据库表取值
 *
 * 使用懒加载
 */
public class SystemProperty {

    private static ApplicationContext _applicationContext;

    private static Environment _environment;

    private static Map<String, Object> propertyMap = new HashMap<>();

    public static void initComConfig(ApplicationContext applicationContext) {
        _applicationContext = applicationContext;
        _environment = applicationContext.getEnvironment();
    }

    public static void clearCacheMap() {
        propertyMap.clear();
    }

    /**
     * @return the "ComConfigDao" bean from the application context
     */
    private static ComConfigDao getComConfigDao() {
        return _applicationContext.getBean(ComConfigDao.class);
    }

    public static <T> T resolveProperty(String name, Object defaultVal, Class<T> clz) {
        Object rst = propertyMap.get(name);
        if (rst == null) {
            if (_environment.acceptsProfiles("dev")) {
                rst = StringUtils.trimToNull(_environment.getProperty(name));
                if (rst == null) {
                    rst = getComConfigDao().getProperty(name);
                }
            } else {
                rst = getComConfigDao().getProperty(name);
                if (rst == null) {
                    rst = StringUtils.trimToNull(_environment.getProperty(name));
                }
            }
            if (rst != null) {
                propertyMap.put(name, rst);
            }
        }
        return (T) rst;
    }

    public static String resolveStringProperty(String name) {
        Object rst = propertyMap.get(name);
        if (rst == null) {
            if (_environment.acceptsProfiles("dev")) {
                rst = StringUtils.trimToNull(_environment.getProperty(name));
                if (rst == null) {
                    rst = getComConfigDao().getProperty(name);
                }
            } else {
                rst = getComConfigDao().getProperty(name);
                if (rst == null) {
                    rst = StringUtils.trimToNull(_environment.getProperty(name));
                }
            }
            if (rst != null) {
                propertyMap.put(name, rst);
            }
        }
        return (String) rst;
    }

    public static String resolveStringProperty(String name, String defaultValue) {
        String rst = resolveStringProperty(name);
        if (rst == null || rst.trim().length() == 0) {
            return defaultValue;
        }
        return rst;
    }

    public static int resolveIntProperty(String name) {
        return NumberUtils.toInt(resolveStringProperty(name));
    }
    public static int resolveIntProperty(String name, int defaultValue) {
        return NumberUtils.toInt(resolveStringProperty(name), defaultValue);
    }

    public static boolean resolveBooleanProperty(String name) {
        return BooleanUtils.toBoolean(resolveStringProperty(name));
    }
    public static boolean resolveBooleanProperty(String name, boolean defaultValue) {
        return BooleanUtils.toBoolean(resolveStringProperty(name));
    }

    public static long resolveLongProperty(String name) {
        return NumberUtils.toLong(resolveStringProperty(name));
    }
    public static long resolveLongProperty(String name, long defaultValue) {
        return NumberUtils.toLong(resolveStringProperty(name), defaultValue);
    }

}
