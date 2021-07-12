package org.example.common.sql;


import org.example.common.pojo.SqlParam;

/**
 * @date 2020/12/23 15:24.
 */
public class SqlCompare extends WhereStatement<SqlCompare> {

    @Override
    protected void doBuild(StringBuilder sqlBuilder) {
        String whereSql = buildWhereSegment(true);
        sqlBuilder.append(whereSql);
    }

    @Override
    protected String addAlias(String columnName) {
        return columnName;
    }

    @Override
    protected String chooseColumnName(SqlParam sqlParam) {
        return sqlParam.getColumnName();
    }
}
