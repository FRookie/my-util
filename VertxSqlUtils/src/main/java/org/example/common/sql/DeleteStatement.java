package org.example.common.sql;

/**
 */
public class DeleteStatement extends BaseStatement {

    private static final String SQL_DELETE = "DELETE";


    public DeleteStatement(Class clz) {
        super(clz);
    }

    @Override
    protected void doBuild(StringBuilder sqlBuilder) {
        sqlBuilder.append(buildDeleteSegement()).append(SQL_SPACE);
        String whereSegment = buildWhereSegment(false);
        sqlBuilder.append(whereSegment);
    }

    private String buildDeleteSegement() {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(SQL_DELETE).append(SQL_SPACE);
        String formSegment = buildFromSegment();
        String tableSegment = buildTableSegment();
        sqlBuilder.append(formSegment).append(SQL_SPACE);
        sqlBuilder.append(tableSegment).append(SQL_SPACE);
        return sqlBuilder.toString();
    }
}
