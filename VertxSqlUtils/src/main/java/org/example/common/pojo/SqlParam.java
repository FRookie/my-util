package org.example.common.pojo;


import org.example.common.enums.SqlPattern;

/**
 * Sql参数
 *
 * @date 2020/12/21 17:44.
 */
public class SqlParam {

    private SqlPattern pattern;
    private String paramName;
    private Object paramValue;
    private String columnName;

    public SqlParam(SqlPattern pattern, String paramName) {
        this.pattern   = pattern;
        this.paramName = paramName;
    }
    public SqlParam(String paramName, Object paramValue) {
        this.paramName  = paramName;
        this.paramValue = paramValue;
    }
    //不开放直接设置ColumnName的构造函数
    //public SqlParam(SqlPattern pattern, String paramName, String columnName)
    public SqlPattern getPattern() {
        return pattern;
    }

    public String getParamName() {
        return paramName;
    }

    public Object getParamValue() {
        return paramValue;
    }

    public String getColumnName() {
        return columnName;
    }

    public SqlParam setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public SqlParam setParamValue(Object paramValue) {
        this.paramValue = paramValue;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SqlParam param = (SqlParam) o;

        return paramName.equals(param.paramName);
    }

    @Override
    public int hashCode() {
        return paramName.hashCode();
    }

    @Override
    public String toString() {
        return "SqlParam{" +
            "pattern=" + pattern +
            ", paramName='" + paramName + '\'' +
            ", paramValue=" + paramValue +
            ", columnName='" + columnName + '\'' +
            '}';
    }
}
