package org.example.common.sql;


import org.example.common.constant.OrderBy;
import org.example.common.enums.SqlPattern;
import org.example.common.pojo.SqlParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.example.common.constant.OrderBy.ASC;
import static org.example.common.constant.OrderBy.DESC;


/**
 * @date 2020/12/21 17:56.
 */
public class SelectStatement extends BaseStatement {

    Logger logger = LoggerFactory.getLogger(getClass());

    private static final String SQL_ORDER_BY = "order by";
    protected String pageCountSql;
    private Integer totalCount = 15;
    private Map<String, SqlParam> pageParamMap = new HashMap<>();
    private Set<SqlParam> orderByParamSet = new LinkedHashSet<>();
    private boolean isPageQuery = false;

    public SelectStatement(Class clz) {
        super(clz);
        pageParamMap.put(SQL_PAGE_NUM, new SqlParam(SqlPattern.limit, SQL_PAGE_NUM).setParamValue(1));
        pageParamMap.put(SQL_PAGE_SIZE, new SqlParam(SqlPattern.limit, SQL_PAGE_SIZE).setParamValue(15));
    }

    @Override
    public SelectStatement page(int pageNum, int pageSize, int totalCount) {
        if (pageNum <= 0 || pageSize <= 0) {
            return this;
        }
        pageParamMap.get(SQL_PAGE_NUM).setParamValue(pageNum);
        pageParamMap.get(SQL_PAGE_SIZE).setParamValue(pageSize);
        this.totalCount  = totalCount;
        this.isPageQuery = true;
        return this;
    }

    @Override
    public SelectStatement orderByParams(SqlParam... sqlParam) {
        if (sqlParam.length <= 0) {
            return this;
        }
        return orderByParams(new LinkedHashSet<>(Arrays.asList(sqlParam)));
    }

    @Override
    public SelectStatement orderByParams(LinkedHashSet<SqlParam> sqlParamSet) {
        orderByParamSet.addAll(sqlParamSet);
        return this;
    }

    @Override
    protected void doBuild(StringBuilder sqlBuilder) {
        requiredNotEmpty(columnSet);
        //select 字段
        sqlBuilder.append(buildSelectSegment()).append(SQL_SPACE);
        //column
        sqlBuilder.append(mergeBuildColumnSegment()).append(SQL_SPACE);
        //from
        sqlBuilder.append(buildFromSegment()).append(SQL_SPACE);
        //table
        sqlBuilder.append(mergeBuildTableSegment()).append(SQL_SPACE);
        //where
        if (!this.hasBuilderWhere) {
            this.whereSegment = buildWhereSegment(true);
        }
        sqlBuilder.append(this.whereSegment).append(SQL_SPACE);
        //oder by
        sqlBuilder.append(buildOrderBySegment()).append(SQL_SPACE);
        //分页limit
        if (this.isPageQuery) {
            sqlBuilder.append(buildLimitSegment()).append(SQL_SPACE);
        }
    }

    /**
     * 构建分页查询总数的sql，如select count(1) from user where id=?
     */
    @Override
    public String buildPageCountSql() {
        StringBuilder pageSqlBuilder = new StringBuilder();
        pageSqlBuilder.append(SQL_SELECT).append(SQL_SPACE);
        pageSqlBuilder.append(SQL_COUNT).append(SQL_SPACE);
        //from
        pageSqlBuilder.append(buildFromSegment()).append(SQL_SPACE);
        //table
        pageSqlBuilder.append(mergeBuildTableSegment()).append(SQL_SPACE);
        //where
        String whereSegment = buildWhereSegment(true);
        pageSqlBuilder.append(whereSegment);
        this.pageCountSql = pageSqlBuilder.toString();

        logger.debug("SQL statement: {}", pageSqlBuilder.toString());
        logger.debug("SQL Param：{}", this.getTuple().deepToString());

        return pageSqlBuilder.toString();
    }

    protected String buildLimitSegment() {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(SQL_NEW_LINE);
        sqlBuilder.append(SQL_LIMIT).append(SQL_SPACE);
        sqlBuilder.append(SQL_PLACE_HOLDER).append(SQL_SPACE);
        sqlBuilder.append(SQL_COMMA).append(SQL_SPACE);
        sqlBuilder.append(SQL_PLACE_HOLDER).append(SQL_SPACE);
        Integer pageNumValue = (Integer) pageParamMap.get(SQL_PAGE_NUM).getParamValue();
        Integer pageSizeValue = (Integer) pageParamMap.get(SQL_PAGE_SIZE).getParamValue();
        int offset = calculateMin(pageNumValue, pageSizeValue);
        int pageSize = calculateMax(pageNumValue, pageSizeValue);
        this.getTuple().addValue(offset);
        this.getTuple().addValue(pageSize);
        return sqlBuilder.toString();
    }

    protected String buildOrderBySegment() {
        StringBuilder sqlBuilder = new StringBuilder();
        String orderByExpression = mergeBuildOrderByExpression();
        if (!SQL_BLANK.equals(orderByExpression)) {
            sqlBuilder.append(SQL_NEW_LINE).append(SQL_ORDER_BY).append(SQL_SPACE);
            sqlBuilder.append(orderByExpression).append(SQL_SPACE);
        }
        return sqlBuilder.toString();
    }

    protected String mergeBuildOrderByExpression() {
        return buildOrderByExpression();
    }

    protected String buildOrderByExpression() {
        StringBuilder orderByColumnBuilder = new StringBuilder();
        boolean isBlank = true;
        for (SqlParam sqlParam : orderByParamSet) {
            if (!isBlank) {
                orderByColumnBuilder.append(SQL_COMMA).append(SQL_SPACE);
            }
            String paramValue = (String) Optional
                .ofNullable(handlerParamValue(sqlParam.getParamValue()))
                .orElse(SQL_BLANK);
            if (isOrderByValueLegal(paramValue)) {
                String columnName = chooseColumnName(sqlParam);
                if (!SQL_BLANK.equals(columnName)) {
                    columnName = addAlias(columnName);
                    orderByColumnBuilder.append(columnName).append(SQL_SPACE);
                    orderByColumnBuilder.append(paramValue).append(SQL_SPACE);
                    isBlank = false;
                }
            } else if (OrderBy.SPECIFY.equalsIgnoreCase(paramValue)) {
                orderByColumnBuilder.append(sqlParam.getParamName()).append(SQL_SPACE);
                isBlank = false;
            }
        }
        return orderByColumnBuilder.toString();
    }

    protected String buildSelectSegment() {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(SQL_SELECT).append(SQL_SPACE);
        return sqlBuilder.toString();
    }

    /**
     * 预留关联查询覆写
     */
    protected String mergeBuildColumnSegment() {
        return buildColumnSegment();
    }

    /**
     * 根据columnSet拼接select部分的字段
     */
    protected String buildColumnSegment() {
        requiredNotEmpty(this.columnSet);
        requiredIdInColumnSet(this.columnSet);
        StringBuilder sqlBuilder = new StringBuilder();
        //StringBuilder sqlBuilder = new StringBuilder();
        boolean isBlank = true;
        for (String column : this.columnSet) {
            if (!isBlank) {
                sqlBuilder.append(SQL_COMMA);
            }
            sqlBuilder.append(addAliasAndPrefix(column)).append(SQL_SPACE);
            isBlank = false;
        }
        return sqlBuilder.toString();
    }

    /**
     * 预留关联查询覆写
     */
    protected String mergeBuildTableSegment() {
        return buildTableSegment();
    }

    public String getPageCountSql() {
        return pageCountSql;
    }

    //###########私有方法

    private int calculateMin(int pageNum, int pageSize) {
        return (pageNum - 1) * pageSize;
    }

    private int calculateMax(Integer pageNum, Integer pageSize) {
//        int expectedMax = pageNum * pageSize;
        if (this.totalCount == 0) {
            return 0;
        }
        int remainCount = totalCount - (pageNum - 1) * pageSize;
//        return expectedMax > this.totalCount ? -1 : pageSize;
        return remainCount > pageSize ? pageSize : remainCount;
    }

    private boolean isOrderByValueLegal(String paramValue) {
        return DESC.equalsIgnoreCase(paramValue) ||
            ASC.equalsIgnoreCase(paramValue);
    }
}
