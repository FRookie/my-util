package org.example.common.util.reflect;

import software.amazon.awssdk.utils.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @date 2021/4/1 10:03.
 */
public class ReflectUtils {

    /**
     * 递归反射获取属性值
     * 如 a.b.c  如果其中的属性为数组或集合直接返回
     */
    public static Object readPropertyDeep(Object obj, String fullName) {
        if (StringUtils.isBlank(fullName)) {
            throw new IllegalArgumentException("fullName can not blank");
        }
        if (Objects.isNull(obj)) {
            return null;
        }
        if (!isMappable(obj)) {
            return obj;
        }
        Object propertyValue;
        String propertyName;
        String children = null;
        int docIndex = fullName.indexOf(".");
        if (docIndex != -1) {
            propertyName = fullName.substring(0, docIndex);
            children     = fullName.substring(docIndex + 1, fullName.length());
        } else {
            propertyName = fullName;
        }
        try {
            propertyValue = readProperty(obj, propertyName);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalArgumentException("can not read property by " + fullName + " in class" + obj.getClass());
        }
        return StringUtils.isNotBlank(children) ? readPropertyDeep(propertyValue, children) : propertyValue;
    }

    private static Object readProperty(Object obj, String propertyName) throws NoSuchFieldException, IllegalAccessException {
        Object propertyValue;
        Field field = obj.getClass().getDeclaredField(propertyName);
        field.setAccessible(true);
        propertyValue = field.get(obj);
        return propertyValue;
    }

    /**
     * 判断能否反射读取属性
     */
    private static boolean isMappable(Object value) {
        return !isArray(value) && !isCollection(value);
    }

    //判断对象是否为数组
    public static boolean isArray(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj.getClass().isArray();
    }

    /**
     * 判断是否为集合类型
     */
    public static boolean isCollection(Object obj) {
        if (obj == null) {
            return false;
        }
        return Collection.class.isAssignableFrom(obj.getClass()) ||
            Map.class.isAssignableFrom(obj.getClass());
    }

    protected Class<?> resolveInterface(Class<?> type) {
        Class<?> classToCreate;
        if (type == List.class || type == Collection.class || type == Iterable.class) {
            classToCreate = ArrayList.class;
        } else if (type == Map.class) {
            classToCreate = HashMap.class;
        } else if (type == SortedSet.class) {
            classToCreate = TreeSet.class;
        } else if (type == Set.class) {
            classToCreate = HashSet.class;
        } else {
            classToCreate = type;
        }
        return classToCreate;
    }


}
