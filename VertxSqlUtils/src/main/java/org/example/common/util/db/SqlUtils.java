package org.example.common.util.db;


import org.example.common.annotation.Column;
import org.example.common.annotation.Table;
import org.example.common.exception.IAException;
import org.example.common.pojo.SqlParam;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

/**
 * @date 2020/12/22 9:48.
 */
public class SqlUtils {

    /**
     * 获取类中所有字段的字段，
     * 1、依据注解转为需要的字段名称
     * 2、或直接驼峰转下划线
     *
     * @param clz 类
     * @return 所有字段集合
     */
    public static <T> Set<String> clzFieldToColumnSet(Class<T> clz) {
        Set<String> columnSet = new LinkedHashSet<>();
        Field[] fields = clz.getDeclaredFields();
        for (Field field : fields) {
            if (!isSerializableId(field)) {
                //是否自动排除map和colleciton
                String column = fieldToColumn(field);
                columnSet.add(column);
            }
        }
        return columnSet;
    }

    /**
     * 1、获取类中对应字段的Column注解
     * 2、或者字段名驼峰转下划线
     * 3、没有对应字段返回空字符串
     *
     * @param clz       类
     * @param fieldName 字段名称
     */
    public static String getColumnByFieldName(Class clz, String fieldName) {
        try {
            Field declaredField = clz.getDeclaredField(fieldName);
            return fieldToColumn(declaredField);
        } catch (NoSuchFieldException e) {
            //e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取字段上Column注解的值,或对field字段名称驼峰转下划线
     */
    public static String fieldToColumn(Field field) {
        String fieldName = field.getName();
        Column annotation = field.getAnnotation(Column.class);
        return annotation == null ? camelToUnderline(fieldName) : annotation.value();
    }

    /**
     * 判断字段是否为序列化字段
     */
    public static boolean isSerializableId(Field field) {
        return "serialVersionUID".equals(field.getName());
    }

    /**
     * 或者指定类的表名
     * 1、Table注解指定
     * 2、类名驼峰转换
     *
     * @return 小写表名
     */
    public static String clzTableName(Class<?> clz) {
        Table declaredAnnotation = clz.getAnnotation(Table.class);
        String tableName;
        if (declaredAnnotation != null) {
            tableName = declaredAnnotation.value();
        } else {
            tableName = camelToUnderline(clz.getSimpleName());
        }
        return tableName.toLowerCase();
    }

    /**
     * 驼峰转下划线
     *
     * @param fieldName 字段名
     * @return 下划线形式字段名
     */
    public static String camelToUnderline(String fieldName) {
        if (fieldName == null) {
            return null;
        }
        char[] charArr = fieldName.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < charArr.length; i++) {
            char c = charArr[i];
            if (Character.isUpperCase(c)) {
                if (i == 0) {
                    sb.append((char) (c + 32));
                    continue;
                }
                sb.append("_");
                sb.append((char) (c + 32));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 获取表名
     */
    public static String getTableName(Class<?> clazz) {
        String tableName;
        if (clazz.isAnnotationPresent(Table.class)) {
            tableName = clazz.getAnnotation(Table.class).value();
        } else {
            tableName = SqlUtils.camelToUnderline(clazz.getSimpleName());
        }
        return tableName;
    }


    /**
     * 根据传入对象生成，BaseStatement装为Set<SqlParam>
     */
    public static Set<SqlParam> generateSqlParamList(Object paramObject) {
        Set<SqlParam> paramSet = new HashSet<>();
        if (paramObject instanceof Iterable) {
            Iterable iterable = (Iterable) paramObject;
            iterable.forEach(paramIterable -> {
                Set<SqlParam> sqlParams = generateSqlParamList(paramIterable);
                paramSet.addAll(sqlParams);
            });
        } else if (paramObject instanceof Map) {
            Map paramMap = (Map) paramObject;
            paramMap.entrySet().forEach(paramIterable -> {
                Set<SqlParam> sqlParams = generateSqlParamList(paramIterable);
                paramSet.addAll(sqlParams);
            });
        } else if (paramObject instanceof Map.Entry) {
            Entry param = (Entry) paramObject;
            Object value = param.getValue();
            if (value == null) {
                paramSet.add(new SqlParam(param.getKey().toString(), value));
            } else if (value instanceof Iterable) {
                Iterable iterable = (Iterable) value;
                iterable.forEach(paramIterable -> {
                    Set<SqlParam> sqlParams = generateSqlParamList(paramIterable);
                    paramSet.addAll(sqlParams);
                });
            } else if (value instanceof Map) {
                Map paramMap = (Map) value;
                paramMap.entrySet().forEach(paramIterable -> {
                    Set<SqlParam> sqlParams = generateSqlParamList(paramIterable);
                    paramSet.addAll(sqlParams);
                });
            } else if (isBasicType(value)) {
                SqlParam sqlParam = new SqlParam((String) param.getKey(), value);
                paramSet.add(sqlParam);
            } else {
                paramSet.addAll(handleValueFromModel(value));
            }
        } else {
            paramSet.addAll(handleValueFromModel(paramObject));
        }
        return paramSet;
    }

    /**
     * 将model对象转为SqlParam集合
     *
     * @param paramObject model对象
     */
    private static Set<SqlParam> handleValueFromModel(Object paramObject) {
        Set<SqlParam> paramSet = new HashSet<>();
        Field[] fieldArr = paramObject.getClass().getDeclaredFields();
        for (Field field : fieldArr) {
            field.setAccessible(true);
            if ("serialVersionUID".equals(field.getName())) {
                continue;
            }
            Object value;
            try {
                value = field.get(paramObject);
            } catch (IllegalAccessException e) {
                throw new IAException("参数非法");
            }
            paramSet.add(new SqlParam(field.getName(), value));
        }
        return paramSet;
    }

    /**
     * 判断对象是否时基本类型
     */
    private static boolean isBasicType(Object value) {
        return value instanceof Number ||
            value instanceof Boolean ||
            value instanceof String;
    }

    public static String lowercaseAndUnderlineToCamel(String fieldName) {
        return convertToCamel(fieldName,true);
    }

    /**
     * 下划线转驼峰
     */
    public static String underlineToCamel(String fieldName) {
        return convertToCamel(fieldName,false);
    }

    private static String convertToCamel(String fieldName, boolean toLowerCase) {
        if (fieldName == null) {
            return null;
        }
        char[] charArr =null;
        if(toLowerCase){
            charArr = fieldName.toLowerCase().toCharArray();
        }else {
            charArr = fieldName.toCharArray();
        }

        StringBuilder sb = new StringBuilder();
        boolean isUnderline = false;
        for (int i = 0; i < charArr.length; i++) {
            char c = charArr[i];
            if (Objects.equals('_', c)) {
                isUnderline = true;
            } else if (isUnderline) {
                isUnderline = false;
                sb.append((char) (c - 32));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
