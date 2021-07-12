package org.example.common.util;

import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import org.example.common.annotation.Column;
import org.example.common.pojo.JoinResult;
import org.example.common.util.db.SqlUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * model工具类
 */
public class ModelUtils {

    public static <T> Integer rowSetToInteger(RowSet<Row> rowSet) {
        RowIterator<Row> iterator = rowSet.iterator();
        if (iterator.hasNext()) {
            Row row = iterator.next();
            return row.getInteger(0);
        }
        return 0;
    }

    public static <T> T rowSetToModel(RowSet<Row> rowSet, Class<T> clz) {
        List<T> modelList = rowSetToList(rowSet, clz);
        return modelList != null && modelList.size() > 0 ? modelList.get(0) : null;
    }

//    public static <T> List<T> rowSetToList(RowSet<Row> rowSet, Class<T> clz) {
//        ArrayList<T> modelList = new ArrayList<>(rowSet.size());
//        for (Row row : rowSet) {
//            T model = rowToModel(row, clz);
//            modelList.add(model);
//        }
//        return modelList;
//    }

    /**
     * 将查询结果的rowSet转化为实体类List集合
     */
    public static <T> List<T> rowSetToList(RowSet<Row> rowSet, Class<T> clz) {
        ArrayList<T> modelList = new ArrayList<>(rowSet.size());
        try {

            // 获取当前实体类的可用的 field 对应 获取字段值的方法的map
            Map<Field, Method> field2MethodMap = new HashMap<>();
            // 获取当前实体类的可用的 field 对应 字段名的map
            Map<Field, String> field2DbColNameMap = new HashMap<>();

            Field[] fields = clz.getDeclaredFields();

            // 操作rowSet中的第一个对象, 初始化field2MethodMap
            if (rowSet.iterator().hasNext()) {
                Row firstRow = rowSet.iterator().next();
                // 遍历field 初始化两个map
                for (Field field : fields) {
                    // 字段名
                    String dbColName = "";
                    Class fieldType = field.getType();
                    field.setAccessible(true);

                    // 此field 是序列id则跳过
                    if ("serialVersionUID".equals(field.getName())) {
                        continue;
                    }

                    // 设置dbColName, 此field是否有Column注解,有则使用注解名称, 没有则转驼峰
                    if (field.isAnnotationPresent(Column.class)) {
                        dbColName = field.getAnnotation(Column.class).value();
                    } else {
                        dbColName = SqlUtils.camelToUnderline(field.getName());
                    }

                    // 判断 每一行中是否有该字段 没有则跳过
                    if (-1 == firstRow.getColumnIndex(dbColName)) {
                        continue;
                    }

                    Method getRowValueMethod = Row.class.getMethod("get" + fieldType.getSimpleName(), String.class);
                    // 初始化两个map
                    field2MethodMap.put(field, getRowValueMethod);
                    field2DbColNameMap.put(field, dbColName);
                }
            }

            // 遍历rowSet进行 row到entity的转换
            for (Row row : rowSet) {
                T entity = clz.getDeclaredConstructor().newInstance();
                for (Field field : field2DbColNameMap.keySet()) {
                    field.set(entity, field2MethodMap.get(field).invoke(row, field2DbColNameMap.get(field)));
                }
                modelList.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return modelList;
    }

    private static <T> T rowToModel(Row row, Class<T> clazz) {
        T t = null;
        try {
            t = clazz.getDeclaredConstructor().newInstance();

            Field[] fieldArray = clazz.getDeclaredFields();

            // 遍历 fields
            for (Field field : fieldArray) {
                String dbColName = "";
                Class fieldType = field.getType();
                field.setAccessible(true);

                // 此field 是序列id则跳过
                if ("serialVersionUID".equals(field.getName())) {
                    continue;
                }

                // 设置dbColName, 此field是否有Column注解,有则使用注解名称, 没有则转驼峰
                if (field.isAnnotationPresent(Column.class)) {
                    dbColName = field.getAnnotation(Column.class).value();
                } else {
                    dbColName = SqlUtils.camelToUnderline(field.getName());
                }

                // 判断 每一行中是否有该字段 没有则跳过
                if (-1 == row.getColumnIndex(dbColName)) {
                    continue;
                }

                // 方法getId
                Method getRowValueMethod = Row.class.getMethod("get" + fieldType.getSimpleName(), String.class);
                field.set(t, getRowValueMethod.invoke(row, dbColName));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return t;
    }

    /**
     * 查询结果转为jsonobject的集合，如果clzL和clzR字段名相同，那么保留clzL的值
     *
     * @param rowSet 查询结果
     * @param clzL   查询主表
     * @param clzR   查询关联表
     */
    public static <L, R> List<JsonObject> rowSetToObjectList(RowSet<Row> rowSet, Class<L> clzL, Class<R> clzR) {
        List<JsonObject> result = new ArrayList<>();
        Map<String, String> columnMapL = getPrefixColumnSet(clzL);
        Map<String, String> columnMapR = getPrefixColumnSet(clzR);
        columnMapR.forEach(columnMapL::putIfAbsent);

        for (Row row : rowSet) {
            JsonObject rowObject = new JsonObject();
            columnMapL.forEach((fieldName, column) -> {
                try {
                    rowObject.put(fieldName, row.getValue(column));
                } catch (Exception e) {
                }
            });
            result.add(rowObject);
        }
        return result;
    }

    /**
     * 通常右边只是取部分字段，为保证性能，增加参数指明次表需要的字段
     */
    public static <L, R> List<JsonObject> rowSetToObjectList(RowSet<Row> rowSet, Class<L> clzL, Class<R> clzR,
                                                             List<String> clzRIncludeField) {
        Objects.requireNonNull(clzRIncludeField);
        List<JsonObject> result = new ArrayList<>();
        Map<String, String> columnMapL = getPrefixColumnSet(clzL);
        Map<String, String> columnMapR = getPrefixColumnSet(clzR);
        for (String field : clzRIncludeField) {
            String value = columnMapR.get(field);
            if (value != null) {
                columnMapL.putIfAbsent(field, value);
            }
        }
        for (Row row : rowSet) {
            JsonObject rowObject = new JsonObject();
            columnMapL.forEach((fieldName, column) -> {
                try {
                    rowObject.put(fieldName, row.getValue(column));
                } catch (Exception e) {
                }
            });
            result.add(rowObject);
        }
        return result;
    }

    /**
     * 根据rowSet和设置的别名 进行封装数据
     *
     * @return 封装成List<JsonObject>的数据, 每一个JsonObject都是一行数据
     * @see ModelUtils#rowSet2OriginColumns(io.vertx.sqlclient.RowSet) 替代
     */
    public static List<JsonObject> rowSet2JsonList(RowSet<Row> rowSet, List<String> aliases) {
        List<JsonObject> jsonList = new ArrayList<>();
        for (Row row : rowSet) {
            JsonObject jsonObject = new JsonObject();
            for (String alias : aliases) {
                jsonObject.put(alias, row.getValue(alias));
            }
            jsonList.add(jsonObject);
        }
        return jsonList;
    }

    /**
     * 根据rowSet和设置的别名 进行封装数据
     *
     * @return 封装成List<JsonObject>的数据, 每一个JsonObject都是一行数据
     */
    public static List<JsonObject> rowSet2OriginColumns(RowSet<Row> rowSet) {
        List<JsonObject> jsonList = new ArrayList<>();
        List<String> columnNames = rowSet.columnsNames();
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (String columnName : columnNames) {
            String camel = SqlUtils.underlineToCamel(columnName);
            map.put(columnName, camel);
        }
        for (Row row : rowSet) {
            JsonObject jsonObject = new JsonObject();
            map.forEach((columnName, camelName) -> {
                jsonObject.put(camelName, row.getValue(columnName));
            });
            jsonList.add(jsonObject);
        }
        return jsonList;
    }

    /**
     * 返回key字段名，value为添加前缀的字段名的Map
     */
    private static <L> Map<String, String> getPrefixColumnSet(Class<L> clazz) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        Field[] fieldArray = clazz.getDeclaredFields();
        String prefix = SqlUtils.getTableName(clazz) + "_";
        for (Field field : fieldArray) {
            if (SqlUtils.isSerializableId(field)) {
                continue;
            }
            String dbColName = prefix + SqlUtils.fieldToColumn(field);
            result.put(field.getName(), dbColName);
        }
        return result;
    }

    /**
     * 获取连接查询结果
     * leftjoin 可以导致右表结果全部为null，List需要去掉这部分数据
     */
    public static <T, R> List<JoinResult<T, R>> getJoinResult(RowSet<Row> rowSet, Class<T> oneClass,
                                                              Class<R> manyClass) {
        List<JoinResult<T, R>> list = new ArrayList<>();

        try {
            Iterator<Row> it = rowSet.iterator();
            while (it.hasNext()) {
                JoinResult<T, R> joinResult;
                Row row = it.next();
                T one = prefixRowToModel(row, oneClass);
                R many = prefixRowToModel(row, manyClass);
                int index = indexOf(list, one);
                if (index == -1) {
                    joinResult = new JoinResult<>();
                    joinResult.setOne(one);
                    List<R> manyList = new ArrayList<>();
                    if(many !=null){
                        manyList.add(many);
                    }
                    joinResult.setManyList(manyList);
                    list.add(joinResult);
                } else {
                    joinResult = list.get(index);
                    List<R> manyList = joinResult.getManyList();
                    if(many !=null){
                        manyList.add(many);
                    }
                    joinResult.setManyList(manyList);
                    list.set(index, joinResult);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return list;
        }
    }


    /**
     * Row转Model（Row中columnName带表名前缀）
     *
     * @param clazz 实体T的Class对象
     * @return 实体对象
     */
    private static <T> T prefixRowToModel(Row row, Class<T> clazz) throws Exception {
        T t = clazz.getDeclaredConstructor().newInstance();

        String prefix = SqlUtils.getTableName(clazz) + "_";

        Field[] fieldArray = clazz.getDeclaredFields();

        boolean isNull = true;
        for (Field field : fieldArray) {

            Class fieldType = field.getType();
            field.setAccessible(true);

            if ("serialVersionUID".equals(field.getName())) {
                continue;
            }
            String dbColName = prefix + SqlUtils.fieldToColumn(field);

            try {
                Method getRowValueMethod = Row.class.getMethod("get" + fieldType.getSimpleName(), String.class);
                final Object value = getRowValueMethod.invoke(row, dbColName);
                if (value != null) {
                    isNull = false;
                }
                field.set(t, value);
            } catch (Exception e) {
            }
        }

        return isNull?null:t;
    }


    /**
     * 检索one在list中的位置
     *
     * @param one 实体对象
     * @return one在list中的位置，未找到则返回-1
     */
    private static <T, R> int indexOf(List<JoinResult<T, R>> list, T one) throws Exception {
        Objects.requireNonNull(list);
        Objects.requireNonNull(one);
        int result = -1;
        for (int i = 0; i < list.size(); i++) {
            JoinResult<T, R> jr = list.get(i);
            Field idField = jr.getOne().getClass().getDeclaredField("id");
            Field oneIdField = one.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            oneIdField.setAccessible(true);
            Integer id = (Integer) idField.get(jr.getOne());
            Integer jsonResultId = (Integer) oneIdField.get(one);
            if (id.equals(jsonResultId)) {
                result = i;
            }
        }
        return result;
    }


}

