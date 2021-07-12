package org.example.common.sql;


import org.example.common.enums.SqlPattern;
import org.example.common.pojo.SqlParam;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @date 2021/2/7 11:57.
 */
public abstract class CompareStatement<T extends CompareStatement<T>> implements Compare {


    protected ArrayList<CompareStatement<T>> childrenAndCompare = new ArrayList<>();
    protected ArrayList<CompareStatement<T>> childrenOrCompare = new ArrayList<>();
    Set<SqlParam> defaultParamSet = new LinkedHashSet<>();
    int count = 0;
    protected T That = (T) this;

    /**
     * 等于 =
     *
     * @param paramName 参数名称
     */
    @Override
    public T eq(String paramName) {
        return addSqlParam(new SqlParam(SqlPattern.eq, paramName));
    }

    /**
     * 等于 =，入参可以为空
     *
     * @param paramName 参数名称
     */
    @Override
    public T eq(String paramName, boolean canNull) {
        if (canNull) {
            return addSqlParam(new SqlParam(SqlPattern.eq_null, paramName));
        }
        return addSqlParam(new SqlParam(SqlPattern.eq, paramName));
    }

    /**
     * 不等于 <>
     *
     * @param paramName 参数名称
     */
    @Override
    public T ne(String paramName) {
        return addSqlParam(new SqlParam(SqlPattern.ne, paramName));
    }

    /**
     * 小于 <
     *
     * @param paramName 参数名称
     */
    @Override
    public T lt(String paramName) {
        return addSqlParam(new SqlParam(SqlPattern.lt, paramName));
    }

    /**
     * 小于 <=
     *
     * @param paramName 参数名称
     */
    @Override
    public T le(String paramName) {
        return addSqlParam(new SqlParam(SqlPattern.le, paramName));
    }

    /**
     * 大于 >
     *
     * @param paramName 参数名称
     */
    @Override
    public T gt(String paramName) {
        return addSqlParam(new SqlParam(SqlPattern.gt, paramName));
    }

    /**
     * 大于等于 >=
     *
     * @param paramName 参数名称
     */
    @Override
    public T ge(String paramName) {
        return addSqlParam(new SqlParam(SqlPattern.ge, paramName));
    }

    /**
     * like '% ? %'
     *
     * @param paramName 参数名称
     */
    @Override
    public T like(String paramName) {
        return addSqlParam(new SqlParam(SqlPattern.Like, paramName));
    }

    /**
     * rlike '? %'
     *
     * @param paramName 参数名称
     */
    @Override
    public T rLike(String paramName) {
        return addSqlParam(new SqlParam(SqlPattern.RLike, paramName));
    }

    /**
     * lLike '% ?'
     *
     * @param paramName 参数名称
     */
    @Override
    public T lLike(String paramName) {
        return addSqlParam(new SqlParam(SqlPattern.LLike, paramName));
    }

    /**
     * in
     *
     * @param paramName 参数名称
     */
    @Override
    public T in(String paramName) {
        return addSqlParam(new SqlParam(SqlPattern.in, paramName));
    }

    /**
     * between
     *
     * @param paramName 参数名称
     */
    @Override
    public T between(String paramName) {
        return addSqlParam(new SqlParam(SqlPattern.between, paramName));
    }

    /**
     * =
     *
     * @param paramName  变量名
     * @param columnName 字段名，如sum(money)
     */
    @Override
    public T eq(String paramName, String columnName) {
        return addSqlParam(new SqlParam(SqlPattern.eq, paramName).setColumnName(columnName));
    }

    /**
     * <>
     *
     * @param paramName  变量名
     * @param columnName 字段名，如sum(money)
     */
    @Override
    public T ne(String paramName, String columnName) {
        return addSqlParam(new SqlParam(SqlPattern.ne, paramName).setColumnName(columnName));
    }

    /**
     * =
     *
     * @param paramName  变量名
     * @param canNull    能否为空
     * @param columnName 字段名，如sum(money)
     */
    @Override
    public T eq(String paramName, boolean canNull, String columnName) {
        if (canNull) {
            return addSqlParam(new SqlParam(SqlPattern.eq_null, paramName).setColumnName(columnName));
        }
        return addSqlParam(new SqlParam(SqlPattern.eq, paramName).setColumnName(columnName));
    }

    /**
     * <
     *
     * @param paramName  变量名
     * @param columnName 字段名，如sum(money)
     */
    @Override
    public T lt(String paramName, String columnName) {
        return addSqlParam(new SqlParam(SqlPattern.lt, paramName).setColumnName(columnName));
    }

    /**
     * <=
     *
     * @param paramName  变量名
     * @param columnName 字段名，如sum(money)
     */
    @Override
    public T le(String paramName, String columnName) {
        return addSqlParam(new SqlParam(SqlPattern.le, paramName).setColumnName(columnName));
    }

    /**
     * >
     *
     * @param paramName  变量名
     * @param columnName 字段名，如sum(money)
     */
    @Override
    public T gt(String paramName, String columnName) {
        return addSqlParam(new SqlParam(SqlPattern.gt, paramName).setColumnName(columnName));
    }

    /**
     * >=
     *
     * @param paramName  变量名
     * @param columnName 字段名，如sum(money)
     */
    @Override
    public T ge(String paramName, String columnName) {
        return addSqlParam(new SqlParam(SqlPattern.ge, paramName).setColumnName(columnName));
    }

    /**
     * like '%?%'
     *
     * @param paramName  变量名
     * @param columnName 字段名，如sum(money)
     */
    @Override
    public T like(String paramName, String columnName) {
        return addSqlParam(new SqlParam(SqlPattern.Like, paramName).setColumnName(columnName));
    }

    /**
     * rlike '?%'
     *
     * @param paramName  变量名
     * @param columnName 字段名，如sum(money)
     */
    @Override
    public T rLike(String paramName, String columnName) {
        return addSqlParam(new SqlParam(SqlPattern.RLike, paramName).setColumnName(columnName));
    }

    /**
     * rlike '%?'
     *
     * @param paramName  变量名
     * @param columnName 字段名，如sum(money)
     */
    @Override
    public T lLike(String paramName, String columnName) {
        return addSqlParam(new SqlParam(SqlPattern.LLike, paramName).setColumnName(columnName));
    }

    /**
     * in
     *
     * @param paramName  变量名
     * @param columnName 字段名，如sum(money)
     */
    @Override
    public T in(String paramName, String columnName) {
        return addSqlParam(new SqlParam(SqlPattern.in, paramName).setColumnName(columnName));
    }

    /**
     * between
     *
     * @param paramName  变量名
     * @param columnName 字段名，如sum(money)
     */
    @Override
    public T between(String paramName, String columnName) {
        return addSqlParam(new SqlParam(SqlPattern.between, paramName).setColumnName(columnName));
    }

    /**
     * or 表达式之间默认使用and连接，如果调用了or则使用or连接
     */
    @Override
    public T or() {
        //不能为null，否则hashcode会报错
        //要考虑空字符串的问题，使用字段名时必须判断是否为空
        return addSqlParam(new SqlParam(SqlPattern.or, UUID.randomUUID().toString()));
    }

    /**
     * or 传入的SqlCompare为一个子集
     */
    @Override
    public T or(Compare sqlCompare) {
        childrenOrCompare.add((T) sqlCompare);
        return That;
    }

    /**
     * new SqlCompare(),并构造构造一个条件集合，最后会返回  and( 条件 )
     */
    @Override
    public T and(Compare sqlCompare) {
        //paramList.addAll(sqlComparation.getParamList());
        childrenAndCompare.add((T) sqlCompare);
        return That;
    }

    protected T addSqlParam(SqlParam sqlParam) {
        defaultParamSet.add(sqlParam);
        return That;
    }

    @Override
    public T eqParam(String paramName, Object paramValue) {
        return addSqlParam(new SqlParam(SqlPattern.eq, paramName).setParamValue(paramValue));
    }
    @Override
    public T neParam(String paramName, Object paramValue) {
        return addSqlParam(new SqlParam(SqlPattern.ne, paramName).setParamValue(paramValue));
    }
    @Override
    public T leParam(String paramName, Object paramValue) {
        return addSqlParam(new SqlParam(SqlPattern.le, paramName).setParamValue(paramValue));
    }
    @Override
    public T ltParam(String paramName, Object paramValue) {
        return addSqlParam(new SqlParam(SqlPattern.lt, paramName).setParamValue(paramValue));
    }
    @Override
    public T geParam(String paramName, Object paramValue) {
        return addSqlParam(new SqlParam(SqlPattern.ge, paramName).setParamValue(paramValue));
    }
    @Override
    public T gtParam(String paramName, Object paramValue) {
        return addSqlParam(new SqlParam(SqlPattern.gt, paramName).setParamValue(paramValue));
    }
    @Override
    public T inParam(String paramName, Object paramValue) {
        return addSqlParam(new SqlParam(SqlPattern.in, paramName).setParamValue(paramValue));
    }
    @Override
    public T likeParam(String paramName, Object paramValue) {
        return addSqlParam(new SqlParam(SqlPattern.Like, paramName).setParamValue(paramValue));
    }
    @Override
    public T rLikeParam(String paramName, Object paramValue) {
        return addSqlParam(new SqlParam(SqlPattern.RLike, paramName).setParamValue(paramValue));
    }
    @Override
    public T lLikeParam(String paramName, Object paramValue) {
        return addSqlParam(new SqlParam(SqlPattern.LLike, paramName).setParamValue(paramValue));
    }
    @Override
    public T betweenParam(String paramName, List paramValue) {
        return addSqlParam(new SqlParam(SqlPattern.between, paramName).setParamValue(paramValue));
    }

    @Override
    public T eqColParam(String colName, Object paramValue) {
        return addSqlParam(new SqlParam(SqlPattern.eq, String.valueOf(count++))
                               .setColumnName(colName)
                               .setParamValue(paramValue));
    }
    @Override
    public T neColParam(String colName, Object paramValue) {
        return addSqlParam(new SqlParam(SqlPattern.ne, String.valueOf(count++))
                               .setColumnName(colName)
                               .setParamValue(paramValue));
    }
    @Override
    public T eqNullColParam(String colName, Object paramValue) {
        return addSqlParam(new SqlParam(SqlPattern.eq_null, String.valueOf(count++))
                               .setColumnName(colName)
                               .setParamValue(paramValue));
    }
    @Override
    public T leColParam(String colName, Object paramValue) {
        return addSqlParam(new SqlParam(SqlPattern.le, String.valueOf(count++))
                               .setColumnName(colName)
                               .setParamValue(paramValue));
    }

    @Override
    public T ltColParam(String colName, Object paramValue) {
        return addSqlParam(new SqlParam(SqlPattern.lt, String.valueOf(count++))
                               .setColumnName(colName)
                               .setParamValue(paramValue));
    }
    @Override
    public T gtColParam(String colName, Object paramValue) {
        return addSqlParam(new SqlParam(SqlPattern.gt, String.valueOf(count++))
                               .setColumnName(colName)
                               .setParamValue(paramValue));
    }
    @Override
    public T geColParam(String colName, Object paramValue) {
        return addSqlParam(new SqlParam(SqlPattern.ge, String.valueOf(count++))
                               .setColumnName(colName)
                               .setParamValue(paramValue));
    }
    @Override
    public T inColParam(String colName, Object paramValue) {
        return addSqlParam(new SqlParam(SqlPattern.in, String.valueOf(count++))
                               .setColumnName(colName)
                               .setParamValue(paramValue));
    }
    @Override
    public T likeColParam(String colName, Object paramValue) {
        return addSqlParam(new SqlParam(SqlPattern.Like, String.valueOf(count++))
                               .setColumnName(colName)
                               .setParamValue(paramValue));
    }
    @Override
    public T rLikeColParam(String colName, Object paramValue) {
        return addSqlParam(new SqlParam(SqlPattern.RLike, String.valueOf(count++))
                               .setColumnName(colName)
                               .setParamValue(paramValue));
    }
    @Override
    public T lLikeColParam(String colName, Object paramValue) {
        return addSqlParam(new SqlParam(SqlPattern.LLike, String.valueOf(count++))
                               .setColumnName(colName)
                               .setParamValue(paramValue));
    }
    @Override
    public T betweenColParam(String colName, List paramValue) {
        return addSqlParam(new SqlParam(SqlPattern.between, String.valueOf(count++))
                               .setColumnName(colName)
                               .setParamValue(paramValue));
    }




    Set<SqlParam> getDefaultParamSet() {
        return defaultParamSet;
    }

    ArrayList<CompareStatement<T>> getChildrenAndCompare() {
        return childrenAndCompare;
    }

    ArrayList<CompareStatement<T>> getChildrenOrCompare() {
        return childrenOrCompare;
    }

    /**
     * 传入的参数集合,用与生成sql的where条件或者update 中set的字段
     * 传入参数的 paramName必须与定义条件时相同
     */
    public T params(Set<SqlParam> inSqlParams) {
        //增加入参判断，因为，有可能前端什么参数也不传
        if (Objects.isNull(inSqlParams) || inSqlParams.isEmpty()) {
            return That;
        }
        recursiveHandlerSqlCompare(this, inSqlParams);
        return That;
    }

    /**
     * 传入的参数数组,用与生成sql的where条件或者update 中set的字段
     */
    public T params(SqlParam... params) {
        if (params.length <= 0) {
            //应该直接返回而不是抛异常
            return That;
        }
        return params(arraysToSet(params));
    }

    protected Set<SqlParam> arraysToSet(SqlParam... params) {
        List<SqlParam> paramList = Arrays.asList(params);
        return new LinkedHashSet<>(paramList);
    }

    /**
     * 递归处理传入的参数
     */
    protected void recursiveHandlerSqlCompare(CompareStatement<T> inSqlCompare, Set<SqlParam> inSqlParams) {
        Set<SqlParam> paramSet = inSqlCompare.getDefaultParamSet();
        setSqlParamValueInSet(paramSet, inSqlParams);
        for (CompareStatement<T> child : inSqlCompare.getChildrenAndCompare()) {
            recursiveHandlerSqlCompare(child, inSqlParams);
        }
        for (CompareStatement<T> child : inSqlCompare.getChildrenOrCompare()) {
            recursiveHandlerSqlCompare(child, inSqlParams);
        }
    }

    private void setSqlParamValueInSet(Set<SqlParam> paramSet,
                                       Set<SqlParam> inSqlParams) {
        Map<String, SqlParam> paramMap = paramSet.stream()
            .collect(Collectors.toMap(SqlParam::getParamName, param -> param));
        for (SqlParam sqlParam : inSqlParams) {
            String fieldName = sqlParam.getParamName();
            SqlParam param = paramMap.get(fieldName);
            if (param != null) {
                param.setParamValue(sqlParam.getParamValue());
            } else {
                addNotExistSqlParam(sqlParam);
            }
        }

    }

    /**
     * 入参不存在在定义的配置信息时的操作，子类扩展
     */
    protected void addNotExistSqlParam(SqlParam sqlParam) {
        //空实现
        //
    }


}
