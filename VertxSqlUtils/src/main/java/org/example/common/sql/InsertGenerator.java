package org.example.common.sql;

import io.vertx.sqlclient.Tuple;
import org.example.common.annotation.Column;
import org.example.common.exception.IAException;
import org.example.common.util.db.SqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @date 2020-12-22
 */
public class InsertGenerator {

    private final static Logger logger = LoggerFactory.getLogger(InsertGenerator.class);

    private static final String SQL_INSERT = "INSERT INTO";
    private static final String SQL_BLANK = " ";
    private static final String SQL_L_BRACKETS = "(";
    private static final String SQL_R_BRACKETS = ")";
    private static final String SQL_VALUE = "VALUES";
    private static final String SQL_QUOTE = "`";

    /**
     * 生成批量insert语句
     *
     * @param list        实体对象集合
     * @param isSelective 是否可选插入（true可选只插入不为null的字段）
     */
    public static Map<String, Object> generateInsertBatchSql(List list, boolean isSelective) {
        try {
            if (list == null || list.size() == 0) {
                return null;
            }
            HashMap<String, Object> map = new HashMap<>();
            StringBuilder sql = new StringBuilder();
            sql.append(SQL_INSERT);
            sql.append(SQL_BLANK);

            Object model = list.get(0);
            String tableName = SqlUtils.getTableName(model.getClass());
            sql.append(tableName);
            sql.append(SQL_BLANK);
            sql.append(SQL_L_BRACKETS);

            String dbColumnName = getColumnNameSql(model, isSelective);
            StringBuilder columnValueSQL = new StringBuilder();
            List<Object> tupleList = new ArrayList<>();
            //拼接占位符？以及Tuple参数
            for (Object obj : list) {
                if (columnValueSQL.length() > 0) {
                    columnValueSQL.append(",");
                }
                columnValueSQL.append(SQL_L_BRACKETS);
                Field[] fieldArr = obj.getClass().getDeclaredFields();
                StringBuilder columnValue = new StringBuilder();
                for (Field field : fieldArr) {
                    field.setAccessible(true);
                    if ("serialVersionUID".equals(field.getName())) {
                        continue;
                    }
                    if (isSelective && field.get(obj) == null) {
                        continue;
                    }
                    if (columnValue.length() > 0) {
                        columnValue.append(",");
                    }
                    columnValue.append("?");
                    tupleList.add(field.get(obj));
                }
                columnValueSQL.append(columnValue);
                columnValueSQL.append(SQL_R_BRACKETS);
            }

            sql.append(dbColumnName);
            sql.append(SQL_R_BRACKETS);
            sql.append(SQL_BLANK);
            sql.append(SQL_VALUE);
            sql.append(SQL_BLANK);
            sql.append(columnValueSQL);

            map.put("sql", sql.toString());
            map.put("tuple", tupleList);
            logger.debug("SQL: {}", sql);
            logger.debug("SQL params: {}", Tuple.tuple(tupleList).deepToString());
            return map;
        } catch (IllegalAccessException e) {
            throw new IAException(e.getMessage());
        }
    }

    /**
     * 获取字段名sql片段
     *
     * @param model       实体对象
     * @param isSelective 是否可选（true不包括值为null的字段）
     * @return 字段名sql片段
     */
    private static String getColumnNameSql(Object model, boolean isSelective) throws IllegalAccessException {
        Class clazz = model.getClass();
        Field[] fieldArr = clazz.getDeclaredFields();
        StringBuilder dbColumnName = new StringBuilder();
        for (Field field : fieldArr) {
            field.setAccessible(true);
            if ("serialVersionUID".equals(field.getName())) {
                continue;
            }
            if (isSelective && field.get(model) == null) {
                continue;
            }
            if (dbColumnName.length() > 0) {
                dbColumnName.append(",");
            }
            String column;
            if (field.isAnnotationPresent(Column.class)) {
                column = field.getAnnotation(Column.class).value();

            } else {
                column = SqlUtils.camelToUnderline(field.getName());
            }
            dbColumnName.append(SQL_QUOTE).append(column).append(SQL_QUOTE);
        }
        return dbColumnName.toString();
    }

}
