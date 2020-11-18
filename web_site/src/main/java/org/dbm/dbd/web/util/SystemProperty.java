package org.dbm.dbd.web.util;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.dbm.dbd.dao.ComConfigDao;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

/**
 * 获取系统配置信息，可以动态刷新
 * 优先从数据库表取值, 取不到值时再从配置文件取值
 * 本地开发环境下，如果配置文件也有值，则覆盖数据库表中取到的值
 *
 * 这里不考虑读取性能问题，不缓存属性值
 */
public class SystemProperty {

    private static ApplicationContext _applicationContext;

    private static Environment _environment;

    public static void initComConfig(ApplicationContext applicationContext) {
        _applicationContext = applicationContext;
        _environment = applicationContext.getEnvironment();
    }

    /**
     * @return the "ComConfigDao" bean from the application context
     */
    private static ComConfigDao getComConfigDao() {
        return _applicationContext.getBean(ComConfigDao.class);
    }

    public static <T> T resolveProperty(String name, Object defaultVal, Class<T> clz) {
        Object rst = getComConfigDao().getProperty(name);
        if (rst == null) {
            // 先从数据库表取值, 取不到值时再从配置文件取值
            rst = StringUtils.trimToNull(_environment.getProperty(name));
            if (rst == null) {
                // 从配置文件取不到值时直接返回设定的缺省值
                return (T) defaultVal;
            }
        } else {
            if (_environment.acceptsProfiles("dev")) {
                // 本地开发环境下，如果配置文件也有值，则覆盖数据库表中取到的值
                Object rst4Dev = StringUtils.trimToNull(_environment.getProperty(name));
                if (rst4Dev != null) {
                    return (T) rst4Dev;
                }
            }
        }

        return (T) rst;
    }

    public static String resolveStringProperty(String name) {
        return resolveProperty(name, null, String.class);
    }

    public static String resolveStringProperty(String name, String defaultValue) {
        return resolveProperty(name, defaultValue, String.class);
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
