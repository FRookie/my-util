package org.example.common.sql;

import java.util.List;

/**
 * @date 2020/12/29 16:51.
 */
public interface Compare {

    Compare eq(String paramName);

    Compare eq(String paramName, boolean canNull);

    Compare ne(String paramName);

    Compare lt(String paramName);

    Compare le(String paramName);

    Compare gt(String paramName);

    Compare ge(String paramName);

    Compare like(String paramName);

    Compare rLike(String paramName);

    Compare lLike(String paramName);

    Compare in(String paramName);

    Compare between(String paramName);

    Compare eq(String paramName, String columnName);

    Compare ne(String paramName, String columnName);

    Compare eq(String paramName, boolean canNull, String columnName);

    Compare lt(String paramName, String columnName);

    Compare le(String paramName, String columnName);

    Compare gt(String paramName, String columnName);

    Compare ge(String paramName, String columnName);

    Compare like(String paramName, String columnName);

    Compare rLike(String paramName, String columnName);

    Compare lLike(String paramName, String columnName);

    Compare in(String paramName, String columnName);

    Compare between(String paramName, String columnName);

    Compare or();

    Compare or(Compare sqlCompare);

    Compare and(Compare sqlCompare);

    Compare eqParam(String paramName, Object paramValue);

    CompareStatement rLikeParam(String paramName, Object paramValue);

    CompareStatement lLikeParam(String paramName, Object paramValue);

    abstract CompareStatement betweenParam(String paramName, List paramValue);

    CompareStatement ltParam(String paramName, Object paramValue);

    CompareStatement geParam(String paramName, Object paramValue);

    CompareStatement gtParam(String paramName, Object paramValue);

    CompareStatement inParam(String paramName, Object paramValue);

    abstract CompareStatement likeParam(String paramName, Object paramValue);

    CompareStatement neParam(String paramName, Object paramValue);

    Compare leParam(String paramName, Object paramValue);

    abstract CompareStatement eqColParam(String colName, Object paramValue);

    CompareStatement rLikeColParam(String colName, Object paramValue);

    CompareStatement lLikeColParam(String colName, Object paramValue);

    Compare betweenColParam(String colName, List paramValue);

    CompareStatement inColParam(String colName, Object paramValue);

    abstract CompareStatement likeColParam(String colName, Object paramValue);

    CompareStatement neColParam(String colName, Object paramValue);

    CompareStatement eqNullColParam(String colName, Object paramValue);

    abstract CompareStatement leColParam(String colName, Object paramValue);

    abstract CompareStatement ltColParam(String colName, Object paramValue);

    abstract CompareStatement gtColParam(String colName, Object paramValue);

    abstract CompareStatement geColParam(String colName, Object paramValue);
}
