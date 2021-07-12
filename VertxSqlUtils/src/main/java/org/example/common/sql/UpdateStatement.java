package org.example.common.sql;


import org.example.common.pojo.SqlParam;

/**
 */
public class UpdateStatement extends BaseStatement {
    protected static final String SQL_UPDATE = "UPDATE";
    protected static final String SQL_SET = "SET";

    public UpdateStatement(Class clz) {
        super(clz);
    }
    @Override
    protected void doBuild(StringBuilder sqlBuilder) {
        requiredNotEmpty(columnSet);
        String subSqlBuilder = buildColumnSegment();
        if (subSqlBuilder.trim().length()>0){
            //update
            sqlBuilder.append(buildUpdateSegment());
            //column
            sqlBuilder.append(subSqlBuilder);
            //where
            sqlBuilder.append(buildWhereSegment(false));
        }
    }

    /**
     * 生成需要更新的字段的部分sql语句
     * @return
     */
    private String buildColumnSegment() {
        StringBuilder subSqlBuilder = new StringBuilder();
        boolean isBlank = true;
        for (SqlParam param : this.getDefaultParamSet()) {
            //做param.getPattern()!=null的判断，
            // 否则设置了patter的变量只能出现在where而无法出现在update set后面
            //如果同时想update set name ="" where name=?，则必须给where的name另起一个变量名
            if(param.getPattern()!=null){
                continue;
            }
            String columnName = chooseColumnName(param);
            //只有在初始化是配置类对应的字段才能能被更新，而不是所有入参都能更新
            if (columnSet.contains(columnName)) {
                if(!isBlank){
                    subSqlBuilder.append(SQL_COMMA).append(SQL_SPACE);
                }
                subSqlBuilder.append(addAlias(columnName)).append(SQL_EQ).append(SQL_PLACE_HOLDER).append(SQL_SPACE);
                this.getTuple().addValue(handlerParamValue(param.getParamValue()));
                isBlank=false;
            }
        }
        return subSqlBuilder.toString();
    }
    private String buildUpdateSegment() {
        StringBuilder sqlBuilder=new StringBuilder();
        sqlBuilder.append(SQL_UPDATE).append(SQL_SPACE);
        //table
        sqlBuilder.append(buildTableSegment()).append(SQL_SPACE);
        //set
        sqlBuilder.append(buildSetSegment()).append(SQL_SPACE);
        return sqlBuilder.toString();
    }
    @Override
    protected void addNotExistSqlParam(SqlParam sqlParam) {
        this.addSqlParam(sqlParam);
    }
    private String buildSetSegment( ) {
        StringBuilder sqlBuilder =new StringBuilder();
        sqlBuilder.append(SQL_SET).append(SQL_SPACE);
        return sqlBuilder.toString();
    }
}
