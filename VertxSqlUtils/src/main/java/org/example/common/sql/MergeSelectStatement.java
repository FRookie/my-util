package org.example.common.sql;

import io.vertx.sqlclient.Tuple;
import org.example.common.util.db.SqlUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @date 2020/12/29 17:52.
 */
public class MergeSelectStatement extends SelectStatement implements Join {


    LinkedList<Statement> statementList = new LinkedList<>();

    public MergeSelectStatement(Class clz) {
        super(clz);
        statementList.add(new Statement(SQL_BLANK, SQL_BLANK, this.clz, this));
    }

    @Override
    protected String mergeBuildColumnSegment() {
        StringBuilder sqlBuilder = new StringBuilder();
        boolean isBlank = true;
        for (Statement statement : this.statementList) {
            String subColumnSegment = statement.getSelectStatement().buildColumnSegment();
            if (!isBlank) {
                sqlBuilder.append(SQL_COMMA).append(SQL_NEW_LINE);
            }
            sqlBuilder.append(subColumnSegment);
            isBlank = false;
        }
        return sqlBuilder.toString();
    }

    @Override
    protected String mergeBuildTableSegment() {
        StringBuilder sqlBuilder = new StringBuilder();
        int size = statementList.size();
        for (int si = 0; si < size; si++) {
            Statement curStatement = statementList.get(si);
            String tableSegment = buildTableSegment(curStatement);
            sqlBuilder.append(tableSegment).append(SQL_SPACE);
            List<KeyPair> curKeyPairList = curStatement.getKeyPairList();
            if (!curKeyPairList.isEmpty()) {
                //on
                String onSegment = buildOnSegment(curStatement);
                sqlBuilder.append(onSegment).append(SQL_SPACE);
            }
        }
        return sqlBuilder.toString();
    }

    protected String buildTableSegment(Statement statement) {
        StringBuilder sqlBuilder = new StringBuilder();
        if (Join.LEFT_JOIN.equals(statement.getType())) {
            sqlBuilder.append(SQL_NEW_LINE).append(LEFT_JOIN).append(SQL_SPACE);
        } else if (Join.INNER_JOIN.equals(statement.getType())) {
            sqlBuilder.append(SQL_NEW_LINE).append(INNER_JOIN).append(SQL_SPACE);
        } else {
        }
        MergeSelectStatement selectStatement = statement.getSelectStatement();
        sqlBuilder.append(selectStatement.getTableName()).append(SQL_SPACE);
        return sqlBuilder.toString();
    }

    private String buildOnSegment(Statement curStatement) {
        StringBuilder sqlBuilder = new StringBuilder();
        List<KeyPair> curKeyPairList = curStatement.getKeyPairList();
        sqlBuilder.append(SQL_ON).append(SQL_SPACE);
        sqlBuilder.append(SQL_LEFT_BRACKET).append(SQL_SPACE);
        boolean isBlank = true;
        for (KeyPair keyPair : curKeyPairList) {
            if (!isBlank) {
                sqlBuilder.append(SQL_AND).append(SQL_SPACE);
            }
            sqlBuilder.append(curStatement.getPrevTableName()).append(SQL_DOT).append(keyPair.getLeftKey())
                .append(SQL_SPACE);
            sqlBuilder.append(SQL_EQ).append(SQL_SPACE);
            sqlBuilder.append(curStatement.getSelectStatement().getTableName()).append(SQL_DOT)
                .append(keyPair.getRightKey())
                .append(SQL_SPACE);
            isBlank = false;
        }
        sqlBuilder.append(SQL_RIGHT_BRACKET).append(SQL_SPACE);
        return sqlBuilder.toString();
    }

    /**
     * 构建表达式，循环构建各个BaseStatement的where条件
     */
    @Override
    protected String mergeBuildExpressionSegment() {
        StringBuilder sqlBuilder = new StringBuilder();
        boolean isBlank = true;
        for (int i = 0; i < statementList.size(); i++) {
            Statement statement = statementList.get(i);
            MergeSelectStatement selectStatement = statement.getSelectStatement();
            String expressionSegment = selectStatement.buildExpressionSegment();
            if (!SQL_BLANK.equals(expressionSegment.trim())) {
                if (!isBlank) {
                    sqlBuilder.append(SQL_NEW_LINE).append(SQL_AND);
                }
                //第二个 selectStatement 开始就要处理tuple
                if (i >= 1) {
                    Tuple curTuple = selectStatement.getTuple();
                    int size = curTuple.size();
                    for (int ci = 0; ci < size; ci++) {
                        Object value = curTuple.getValue(ci);
                        this.getTuple().addValue(value);
                    }
                }

                sqlBuilder.append(expressionSegment);
                isBlank = false;
            }
        }
        return sqlBuilder.toString();
    }

    @Override
    protected String mergeBuildOrderByExpression() {
        StringBuilder sqlBuilder = new StringBuilder();
        boolean isBlank = true;
        for (Statement statement : statementList) {
            MergeSelectStatement selectStatement = statement.getSelectStatement();
            String orderBySegment = selectStatement.buildOrderByExpression();
            if (!isBlank && !SQL_BLANK.equals(orderBySegment.trim())) {
                sqlBuilder.append(SQL_COMMA);
            }
            sqlBuilder.append(orderBySegment);
            isBlank = false;
        }
        return sqlBuilder.toString();
    }

    @Override
    protected String addAliasAndPrefix(String columnName) {
        StringBuilder sqlBuilder = new StringBuilder();
        int di = columnName.indexOf(SQL_DOT);
        if(di>=0){
            return sqlBuilder.append(columnName).append(SQL_SPACE).toString();
        }
        int ci = columnName.indexOf(SQL_LEFT_BRACKET);
        if (ci < 0) {
            sqlBuilder.append(this.tableName).append(SQL_DOT).append(SQL_QUOTE).append(columnName).append(SQL_QUOTE);
        } else {
            sqlBuilder.append(columnName).insert(ci + 1, this.tableName + SQL_DOT).append(SQL_SPACE);
            int ri = columnName.indexOf(SQL_RIGHT_BRACKET);
            if (ri > 0) {
                sqlBuilder.insert(ri,SQL_QUOTE);
            }
        }
        //判断有没有字段内有没有as，如count(id) as id
        String flag = SQL_SPACE + SQL_AS + SQL_SPACE;
        if(columnName.toLowerCase().contains(flag)){
            return sqlBuilder.toString();
        }
        sqlBuilder.append(SQL_AS).append(SQL_SPACE);
        sqlBuilder.append(this.tableName).append(SQL_UNDERLINE).append(columnName);
        return sqlBuilder.toString();
    }


    @Override
    public On leftJoin(BaseStatement selectStatement) {
        Objects.requireNonNull(selectStatement);
        handleJoin((MergeSelectStatement) selectStatement, LEFT_JOIN);
        return this;
    }

    @Override
    public On innerJoin(BaseStatement selectStatement) {
        Objects.requireNonNull(selectStatement);
        handleJoin((MergeSelectStatement) selectStatement, INNER_JOIN);
        return this;
    }

    private void handleJoin(MergeSelectStatement selectStatement, String joinType) {
        Statement prevStatement = statementList.peekLast();
        MergeSelectStatement prevSelectStatemnt = prevStatement.getSelectStatement();
        this.statementList.add(new Statement(joinType, prevSelectStatemnt.getTableName(),
                                             prevSelectStatemnt.getClz(), selectStatement));
    }

    @Override
    public MergeSelectStatement on(String... inKeyPairs) {
        if (inKeyPairs.length <= 0 || inKeyPairs.length % 2 == 1) {
            throw new IllegalArgumentException("The arguments cannot be empty, and the number of arguments must be even");
        }
        Statement statement = statementList.peekLast();
        for (int ki = 0; ki < inKeyPairs.length; ki += 2) {
            String key1 = SqlUtils.getColumnByFieldName(statement.getPrevClz(), inKeyPairs[ki]);
            String key2 = SqlUtils.getColumnByFieldName(statement.getSelectStatement().getClz(), inKeyPairs[ki + 1]);
            if (SQL_BLANK.equals(key1) || SQL_BLANK.equals(key2)) {
                throw new IllegalArgumentException("The ON keyword is not in the corresponding class");
            }
            KeyPair keyPair = new KeyPair(key1, key2);
            statement.addKeyPair(keyPair);
        }
        return this;
    }

    class Statement {

        private String type;
        private String prevTableName;
        private Class prevClz;
        private List<KeyPair> keyPairList = new ArrayList<>();
        private MergeSelectStatement selectStatement;

        public Statement(String type, String prevTableName, Class prevClz, MergeSelectStatement selectStatement) {
            this.type            = type;
            this.prevClz         = prevClz;
            this.prevTableName   = prevTableName;
            this.selectStatement = selectStatement;
        }

        public void addKeyPair(KeyPair keyPair) {
            this.keyPairList.add(keyPair);
        }

        public String getType() {
            return type;
        }

        public Class getPrevClz() {
            return prevClz;
        }

        public String getPrevTableName() {
            return prevTableName;
        }

        public List<KeyPair> getKeyPairList() {
            return keyPairList;
        }

        public MergeSelectStatement getSelectStatement() {
            return selectStatement;
        }
    }

    class KeyPair {

        private String leftKey;
        private String rightKey;

        public KeyPair(String leftKey, String rightKey) {
            this.leftKey  = leftKey;
            this.rightKey = rightKey;
        }

        public String getLeftKey() {
            return leftKey;
        }

        public String getRightKey() {
            return rightKey;
        }
    }

}
