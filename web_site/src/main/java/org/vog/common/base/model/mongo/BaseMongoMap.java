package org.vog.common.base.model.mongo;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.vog.common.util.JacksonUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * BaseMongoMap
 * @author chuanyu.liang, 12/11/15
 * @version 2.0.0
 * @since 2.0.0
 */
public class BaseMongoMap<K, V> extends HashMap<K, V> implements Map<K, V> {

    public <T> T getAttribute(K key) {
        if (key == null) {
            return null;
        } else {
            return (T) super.get(key);
        }
    }

    public void setAttribute(K key, V value) {
        if (value == null) {
            super.remove(key);
        } else {
            super.put(key, value);
        }
    }

    /**
     * 取得子节点的值
     * @param complexKey 节点路径，如 xx.yy.zz, 则调用时为：getSubNode(xx, yy, zz);
     */
    public Object getSubNode(String... complexKey) {
        if (complexKey == null) {
            return null;
        }
        int keyLvl = complexKey.length;
        Object subNode = null;

        for (int i = 0; i < keyLvl; i ++) {
            if (i == 0) {
                subNode = get(complexKey[0]);
            } else {
                if (subNode == null) {
                    return null;
                }
                if (subNode instanceof Map) {
                    subNode = ((Map) subNode).get(complexKey[i]);
                } else {
                    return null;
                }
            }
        }
        return subNode;
    }

    public int getIntAttribute(K key) {
        return convertToInt(getAttribute(key));
    }

    public Long getLongAttribute(K key) {
        return convertToLong(getAttribute(key));
    }

    public double getDoubleAttribute(K key) {
        return convertToDoubel(getAttribute(key));
    }

    public String getStringAttribute(K key) {
        return convertToString(getAttribute(key));
    }

    public Boolean getBooleanAttribute(K key) {
        return convertToBoolean(getAttribute(key));
    }

    @Override
    public String toString() {
        return JacksonUtil.bean2Json(this);
    }

    private int convertToInt(Object input) {
        int result = 0;
        if (input == null) {
            return result;
        }
        if (input instanceof Number) {
            result = ((Number) input).intValue();
        } else {
            if (!StringUtils.isEmpty(input.toString())) {
                result = NumberUtils.toInt(input.toString());
            }
        }
        return result;
    }

    private long convertToLong(Object input) {
        long result = 0;
        if (input == null) {
            return result;
        }
        if (input instanceof Number) {
            result = ((Number) input).longValue();
        } else {
            if (!StringUtils.isEmpty(input.toString())) {
                result = NumberUtils.toLong(input.toString());
            }
        }
        return result;
    }

    private double convertToDoubel(Object input) {
        double result = 0.00;
        if (input == null) {
            return result;
        }
        if (input instanceof Number) {
            result = ((Number) input).doubleValue();
        } else {
            if (!StringUtils.isEmpty(input.toString())) {
                result = NumberUtils.toDouble(input.toString());
            }
        }
        return result;
    }

    private String convertToString(Object input) {
        if (input == null) {
            return null;
        }
        return input.toString();
    }

    private Boolean convertToBoolean(Object input) {
        Boolean result = false;
        if (input == null) {
            return result;
        }
        if (input instanceof Boolean) {
            result = (Boolean) input;
        } else {
            throw new RuntimeException("mongo bool值转换出错 value=" + input.toString());
        }
        return result;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }
}
