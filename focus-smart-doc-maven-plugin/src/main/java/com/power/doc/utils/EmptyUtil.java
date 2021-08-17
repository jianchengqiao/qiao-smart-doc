package com.power.doc.utils;

import java.lang.reflect.*;
import java.util.*;

/**
 * Created by Qiao on 2021/4/23
 */
public class EmptyUtil {
    public static <T> boolean isEmpty(Collection<T> list) {
        return list == null || list.isEmpty();
    }

    public static <T> boolean notEmpty(Collection<T> list) {
        return !isEmpty(list);
    }

    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return map == null || map.isEmpty();
    }

    public static <K, V> boolean notEmpty(Map<K, V> map) {
        return !isEmpty(map);
    }

    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == 0;
    }

    public static <T> boolean notEmpty(T[] array) {
        return !isEmpty(array);
    }

    public static boolean isEmpty(byte[] array) {
        return array == null || array.length == 0;
    }

    public static boolean notEmpty(byte[] array) {
        return !isEmpty(array);
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean notEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isTrimEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean notTrimEmpty(String str) {
        return !isTrimEmpty(str);
    }

    public static boolean anyNull(Object... things) {
        if (things == null || things.length == 0) {
            return true;
        }
        for (Object o : things) {
            if (o == null) {
                return true;
            }
        }
        return false;
    }

    public static boolean noneNull(Object... things) {
        return !anyNull(things);
    }

    public static boolean allNull(Object... things) {
        if (things == null || things.length == 0) {
            return true;
        }
        for (Object o : things) {
            if (o != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * 对象所有成员变量为空
     */
    public static boolean isEmpty(Object obj) {
        Class<?> c = obj.getClass();
        Field[] fields = c.getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value != null) {
                    return false;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 对象任一成员变量不为空
     */
    public static boolean notEmpty(Object obj) {
        return !isEmpty(obj);
    }
}
