package org.example.common.sql;

import org.example.common.pojo.SqlParam;
import org.example.common.util.db.SqlUtils;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @date 2020/12/22 17:47.
 */
public abstract class BaseStatement extends WhereStatement<BaseStatement> implements Join {

    protected final String tableName;
    protected Class clz;
    protected Set<String> columnSet = new LinkedHashSet<>();

    public BaseStatement(Class clz) {
        super();
        Objects.requireNonNull(clz);
        this.clz       = clz;
        this.tableName = SqlUtils.clzTableName(clz);
    }

    /**
     * 获取clz所有字段，根据注解或驼峰转下划线，设置到字段集合中
     * 并排除传入的字段集合
     */
    public BaseStatement columnSetExcept(Collection<String> inColumnSet) {
        Objects.requireNonNull(inColumnSet);
        this.columnSet.clear();
        Set<String> columnSet = SqlUtils.clzFieldToColumnSet(clz);
        columnSet.removeAll(inColumnSet);
        this.columnSet.addAll(columnSet);
        return this;
    }

    /**
     * 设置需要查询的字段
     *
     * @param columnSet select语句中需要查询的字段集合
     */
    public BaseStatement columnSet(Collection<String> columnSet) {
        requiredNotEmpty(columnSet);
        this.columnSet.clear();
        this.columnSet.addAll(columnSet);
        return this;
    }

    /**
     * 设置需要查询的字段，以逗号分隔
     *
     * @param columnSql 以逗号分割的字符串
     */
    public BaseStatement columnSet(String columnSql) {
        Objects.requireNonNull(columnSql);
        String[] split = columnSql.split(",");
        //增加去空格处理
        this.columnSet.addAll(Arrays.stream(split).map(String::trim).collect(Collectors.toList()));
        return this;
    }

    /**
     * 剔除不要查询的字段
     *
     * @param exceptColumnName 不需要查询显示的字段集合
     */
    public BaseStatement without(Collection<String> exceptColumnName) {
        columnSet.removeAll(exceptColumnName);
        return this;
    }

    /**
     * 剔除不要查询的字段
     *
     * @param columnSql 不需要查询显示的字段集合，以逗号分割
     */
    public BaseStatement without(String columnSql) {
        Objects.requireNonNull(columnSql);
        String[] split = columnSql.split(",");
        without(Arrays.stream(split).map(String::trim).collect(Collectors.toList()));
        return this;
    }

    /**
     * 需要查询的字段集合
     *
     * @param additionColumnSet 需要查询的字段集合
     */
    public BaseStatement with(Collection<String> additionColumnSet) {
        requiredNotEmpty(additionColumnSet);
        columnSet.addAll(additionColumnSet);
        return this;
    }

    /**
     * 需要查询的字段集合,以逗号分割
     */
    public BaseStatement with(String columnSql) {
        Objects.requireNonNull(columnSql);
        String[] split = columnSql.split(",");
        with(Arrays.stream(split).map(String::trim).collect(Collectors.toList()));
        return this;
    }

    /**
     * 添加前缀，表面.字段名
     */
    @Override
    protected String addAlias(String columnName) {
        StringBuilder sqlBuilder = new StringBuilder();
        //如果已经添加了.,则认为需要添加前缀
        int di = columnName.indexOf(SQL_DOT);
        if (di > 0) {
            return columnName;
        }
        int ci = columnName.indexOf(SQL_LEFT_BRACKET);
        if (ci < 0) {
            sqlBuilder.append(this.tableName).append(SQL_DOT).append(SQL_QUOTE).append(columnName).append(SQL_QUOTE);
        } else {
            sqlBuilder.append(columnName).insert(ci + 1, this.tableName + SQL_DOT + SQL_QUOTE);
            int ri = sqlBuilder.indexOf(SQL_RIGHT_BRACKET);
            if (ri > 0) {
                sqlBuilder.insert(ri, SQL_QUOTE);
            }
        }
        return sqlBuilder.toString();
    }

    /**
     * 添加前缀以及as 别名
     * 如 表面.字段名 as 表名_字段名
     */
    protected String addAliasAndPrefix(String columnName) {
        return addAlias(columnName);
    }

    protected String buildFromSegment() {
        return SQL_NEW_LINE + SQL_FROM + SQL_SPACE;
    }

    /**
     * 返回clz对应的表名
     */
    protected String buildTableSegment() {
        return this.tableName + SQL_SPACE;
    }


    /**
     * 指定分页第几页和每页大小、select查询的总数
     *
     * @param pageNum    第几页 ，第一页为1
     * @param pageSize   每页的数据量，-1表示到最后
     * @param totalCount 查询的数据总计数
     */
    public BaseStatement page(int pageNum, int pageSize, int totalCount) {
        throw new UnsupportedOperationException("子类实现");
    }

    /**
     * 传入封装后需要orderby的字段，与相应的值，如new SqlParam("id","desc")
     *
     * @param sqlParam orderby的字段与相应的值
     */
    public BaseStatement orderByParams(SqlParam... sqlParam) {
        throw new UnsupportedOperationException("The subclass implementation");
    }

    /**
     * 传入封装后需要orderby的字段集合
     */
    public BaseStatement orderByParams(LinkedHashSet<SqlParam> sqlParamSet) {
        throw new UnsupportedOperationException("The subclass implementation");
    }

    public String buildPageCountSql() {
        throw new UnsupportedOperationException("The subclass implementation");
    }

    @Override
    public On leftJoin(BaseStatement selectStatement) {
        throw new UnsupportedOperationException("The subclass implementation");
    }

    @Override
    public On innerJoin(BaseStatement selectStatement) {
        throw new UnsupportedOperationException("The subclass implementation");
    }

    @Override
    public BaseStatement on(String... keyPairs) {
        throw new UnsupportedOperationException("The subclass implementation");
    }

    /**
     * 选择sqlParam对应的字段名称，没有则返回空字符串
     */
    @Override
    protected String chooseColumnName(SqlParam sqlParam) {
        String columnName;
        //1、sqlParam设置了columnName属性
        //2、对应clz类字段或者其注解上的值
        //3、如果clz里面也没有对应的字段，则直接使用设置的fieldName
        //第三种情况，丢弃,否则会导致多传入参而报错，如果一定要用，请设置columnName
        String paramColumnName = sqlParam.getColumnName();
        if (paramColumnName != null && !SQL_BLANK.equals(paramColumnName)) {
            columnName = paramColumnName;
        } else {
            columnName = SqlUtils.getColumnByFieldName(clz, sqlParam.getParamName());
        }
        return columnName;
    }


    protected void requiredIdInColumnSet(Set<String> columnSet) {
        if (!columnSet.contains("id")) {
            throw new IllegalArgumentException("The collection of fields for SELECT must contain id");
        }
    }


    public Class getClz() {
        return clz;
    }

    String getTableName() {
        return tableName;
    }


}
