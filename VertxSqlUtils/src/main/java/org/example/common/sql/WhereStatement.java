package org.example.common.sql;

import io.vertx.sqlclient.Tuple;
import org.example.common.enums.SqlPattern;
import org.example.common.pojo.SqlParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @date 2021/4/16 13:01.
 */
public abstract class WhereStatement<T extends WhereStatement<T>> extends CompareStatement<T> {

    private Logger logger = LoggerFactory.getLogger(That.getClass());
    protected static final String SQL_SELECT = "SELECT";
    protected static final String SQL_SPACE = " ";
    protected static final String SQL_BLANK = "";
    protected static final String SQL_FROM = "FROM";
    protected static final String SQL_WHERE = "WHERE";
    protected static final String SQL_AND = "and";
    protected static final String SQL_OR = "or";
    protected static final String SQL_PLACE_HOLDER = "?";
    protected static final String SQL_RLIKE = "concat(?,'%')";
    protected static final String SQL_LLIKE = "concat(%,'?')";
    protected static final String SQL_LIKE = "concat('%',?,'%')";
    protected static final String SQL_LEFT_BRACKET = "(";
    protected static final String SQL_RIGHT_BRACKET = ")";
    protected static final String SQL_COMMA = ",";
    protected static final String SQL_EQ = "=";
    protected static final String SQL_INEQUATION = "1=2";
    protected static final String SQL_COUNT = "COUNT(1)";
    protected static final String SQL_LIMIT = "limit";
    protected static final String SQL_PAGE_NUM = "pageNum";
    protected static final String SQL_PAGE_SIZE = "pageSize";
    protected static final String SQL_ON = "on";
    protected static final String SQL_TABLE_PREFIX = "bw";
    protected static final String SQL_AS = "as";
    protected static final String SQL_UNDERLINE = "_";
    protected static final String SQL_DOT = ".";
    protected static final String SQL_NEW_LINE = "\n";
    protected final String SQL_QUOTE = "`";

    protected boolean hasBuilderWhere = false;
    protected String whereSegment;
    protected Tuple tuple = Tuple.tuple();
    private List<PatternHandler> patternHandlerList = new ArrayList<>();

    public WhereStatement() {
        patternHandlerList.add(new SinglePlaceHolderPatternHandler());
        patternHandlerList.add(new LikePatternHandler());
        patternHandlerList.add(new InPatternHandler());
        patternHandlerList.add(new BetweenPatternHandler());
    }

    /**
     * ??????sql??????
     * 1??????select??????columnSet????????????select????????????column??????with
     * 2??????update??????????????????update?????????????????????????????????
     * 3??????update???delete???where?????????????????????????????????where 1=2
     */
    public final String build() {
        StringBuilder sqlBuilder = new StringBuilder();
        doBuild(sqlBuilder);
        String sql = sqlBuilder.toString();
        logger.debug("SQL???{}", sql);
        logger.debug("SQL Param???{}", tuple.deepToString());
        return sql;
    }
    protected abstract void doBuild(StringBuilder sqlBuilder);
    /**
     * ??????where?????????sql???????????????????????????where 1=2
     */
    protected String buildWhereSegment(boolean canNull) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(SQL_NEW_LINE);
        String whereSegment = mergeBuildExpressionSegment();
        if (!SQL_BLANK.equals(whereSegment)) {
            wrapSql(sqlBuilder, whereSegment, SQL_WHERE, SQL_BLANK);
        } else if (!canNull) {
            sqlBuilder.append(SQL_WHERE).append(SQL_SPACE);
            sqlBuilder.append(SQL_INEQUATION).append(SQL_SPACE);
        }
        String whereSql = sqlBuilder.toString();
        this.hasBuilderWhere = true;
        this.whereSegment    = whereSql;
        return whereSql;
    }

    /**
     * ??????where??????
     */
    protected String mergeBuildExpressionSegment() {
        return buildExpressionSegment();
    }

    /**
     * ??????where???????????????recursive??????
     */
    protected String buildExpressionSegment() {
        StringBuilder sqlBuilder = new StringBuilder();
        String expressionSegment = recursiveBuildExpression(this, new AtomicInteger(0));
        return sqlBuilder.append(expressionSegment).toString();
    }

    /**
     * ????????????and??????sql
     *
     * @param inSqlCompare       ?????????Set<SqlParam>????????????????????????
     * @param expressionSetCount ????????????????????????????????????????????????????????????????????????????????????
     */
    private String recursiveBuildExpression(CompareStatement<T> inSqlCompare, AtomicInteger expressionSetCount) {
        //????????????and???????????????????????????
        AtomicBoolean isBlank = new AtomicBoolean(true);
        Set<SqlParam> valueSet = inSqlCompare.getDefaultParamSet();
        StringBuilder sqlBuilder = new StringBuilder();
        String andSegment = buildAndExpression(valueSet);
        if (!SQL_BLANK.equals(andSegment.trim())) {
            wrapSql(sqlBuilder, andSegment, SQL_LEFT_BRACKET, SQL_RIGHT_BRACKET);
            expressionSetCount.incrementAndGet();
            isBlank.set(false);
        }
        //??????and
        buildExpression(sqlBuilder, isBlank, expressionSetCount, inSqlCompare.getChildrenAndCompare(), SQL_AND);
        //??????or
        buildExpression(sqlBuilder, isBlank, expressionSetCount, inSqlCompare.getChildrenOrCompare(), SQL_OR);
        return sqlBuilder.toString();
    }

    protected void wrapSql(StringBuilder sqlBuilder, String segment, String right,
                           String left) {
        sqlBuilder.append(right).append(SQL_SPACE);
        sqlBuilder.append(segment).append(SQL_SPACE);
        sqlBuilder.append(left).append(SQL_SPACE);
    }

    private void buildExpression(StringBuilder sqlBuilder, AtomicBoolean isBlank, AtomicInteger expressionSetCount,
                                 List<CompareStatement<T>> childList, String connectKey) {
        for (CompareStatement<T> child : childList) {
            AtomicInteger subAndCount = new AtomicInteger(0);
            String subAnd = recursiveBuildExpression(child, subAndCount);
            if (!SQL_BLANK.equals(subAnd.trim())) {
                if (!isBlank.get()) {
                    sqlBuilder.append(connectKey).append(SQL_SPACE);
                }
                wrapSql(sqlBuilder,
                        subAnd,
                        subAndCount.get() > 1 ? SQL_LEFT_BRACKET : SQL_BLANK,
                        subAndCount.get() > 1 ? SQL_RIGHT_BRACKET : SQL_BLANK);
                expressionSetCount.incrementAndGet();
                isBlank.set(false);
            }
        }
    }

    private String buildAndExpression(Set<SqlParam> valueSet) {
        //or ????????????????????????????????????
        StringBuilder sqlBuilder = new StringBuilder();
        boolean isBlank = true;
        boolean hasOr = false;
        for (SqlParam sqlParam : valueSet) {
            SqlPattern pattern = sqlParam.getPattern();
            //update??????pattern??????
            if (pattern == null) {
                continue;
            }
            //or???????????????buildComparationSegment??????????????????
            if (pattern.equals(SqlPattern.or)) {
                hasOr = true;
                continue;
            }
            String condition = buildCompareSegment(sqlParam);
            //????????????
            if (!SQL_BLANK.equals(condition.trim())) {
                if (!isBlank && hasOr) {
                    sqlBuilder.append(SQL_OR).append(SQL_SPACE);
                } else if (!isBlank) {
                    sqlBuilder.append(SQL_AND).append(SQL_SPACE);
                }
                sqlBuilder.append(condition).append(SQL_SPACE);
                isBlank = false;
            }
            //??????or?????????????????????????????????
            hasOr = false;
        }
        return sqlBuilder.toString();
    }

    /**
     * ??????sqlParam?????????????????????
     */
    private String buildCompareSegment(SqlParam sqlParam) {
        String columnName = chooseColumnName(sqlParam);
        if (SQL_BLANK.equals(columnName)) {
            return SQL_BLANK;
        }
        //????????????
        columnName = addAlias(columnName);
        //???????????????for????????????Map?????????key??????
        Object value = sqlParam.getParamValue();
        SqlPattern pattern = sqlParam.getPattern();
        if (value != null || pattern.equals(SqlPattern.eq_null)) {
            for (PatternHandler patternHandler : patternHandlerList) {
                if (patternHandler.isSupport(sqlParam)) {
                    return patternHandler.handler(sqlParam, columnName);
                }
            }
        }
        return SQL_BLANK;
    }

    protected <P> void requiredNotEmpty(Collection<P> collection) {
        if (collection == null || collection.isEmpty()) {
            throw new NullPointerException("The collection cannot be empty");
        }
    }

    protected abstract String addAlias(String columnName);

    public Tuple getTuple() {
        return tuple;
    }

    protected abstract String chooseColumnName(SqlParam sqlParam);

    /**
     * ??????????????????????????????????????????collection??????map
     */
    protected Object handlerParamValue(Object paramValue) {
        if (paramValue == null) {
            return null;
        }
        if (paramValue instanceof Collection) {
            Collection collection = (Collection) paramValue;
            if (collection.isEmpty()) {
                return null;
            }
            Iterator iterator = collection.iterator();
            //??????
            return handlerParamValue(iterator.next());
        }
        if (paramValue instanceof Map) {
            Map map = (Map) paramValue;
            if (map.isEmpty()) {
                return null;
            }
            Collection values = map.values();
            Iterator iterator = values.iterator();
            //??????
            return handlerParamValue(iterator.next());
        }
        return paramValue;
    }

    interface PatternHandler {

        /**
         * ???????????????pattern
         */
        boolean isSupport(SqlParam sqlParam);

        /**
         * ??????SqlParam??????????????????string
         */
        String handler(SqlParam sqlParam, String columnName);
    }

    /**
     * ?????????????????????????????????????????????????????????????????????
     */
    class SinglePlaceHolderPatternHandler implements PatternHandler {

        @Override
        public boolean isSupport(SqlParam sqlParam) {
            SqlPattern pattern = sqlParam.getPattern();
            return pattern.equals(SqlPattern.eq) ||
                pattern.equals(SqlPattern.ne) ||
                pattern.equals(SqlPattern.lt) ||
                pattern.equals(SqlPattern.le) ||
                pattern.equals(SqlPattern.gt) ||
                pattern.equals(SqlPattern.ge) ||
                pattern.equals(SqlPattern.eq_null);
        }

        @Override
        public String handler(SqlParam sqlParam, String columnName) {
            StringBuilder sqlBuilder = new StringBuilder();
            SqlPattern pattern = sqlParam.getPattern();
            Object paramValue = sqlParam.getParamValue();
            sqlBuilder.append(columnName).append(SQL_SPACE);
            sqlBuilder.append(pattern.getFlag()).append(SQL_PLACE_HOLDER).append(SQL_SPACE);
            tuple.addValue(handlerParamValue(paramValue));
            return sqlBuilder.toString();
        }
    }

    /**
     * ??????like?????????pattern
     */
    class LikePatternHandler implements PatternHandler {

        @Override
        public boolean isSupport(SqlParam sqlParam) {
            SqlPattern pattern = sqlParam.getPattern();
            return pattern.equals(SqlPattern.Like) ||
                pattern.equals(SqlPattern.LLike) ||
                pattern.equals(SqlPattern.RLike);
        }

        @Override
        public String handler(SqlParam sqlParam, String columnName) {
            StringBuilder sqlBuilder = new StringBuilder();
            SqlPattern pattern = sqlParam.getPattern();
            Object paramValue = sqlParam.getParamValue();
            sqlBuilder.append(columnName).append(SQL_SPACE);
            sqlBuilder.append(pattern.getFlag()).append(SQL_SPACE);
            String likeSegment;
            if (pattern.equals(SqlPattern.RLike)) {
                likeSegment = SQL_RLIKE;
            } else if (pattern.equals(SqlPattern.LLike)) {
                likeSegment = SQL_LLIKE;
            } else {
                likeSegment = SQL_LIKE;
            }
            sqlBuilder.append(likeSegment).append(SQL_SPACE);
            tuple.addValue(handlerParamValue(paramValue));
            return sqlBuilder.toString();
        }
    }

    /**
     * ?????? in ?????????pattern
     */
    class InPatternHandler implements PatternHandler {

        @Override
        public boolean isSupport(SqlParam sqlParam) {
            SqlPattern pattern = sqlParam.getPattern();
            return pattern.equals(SqlPattern.in);
        }

        @Override
        public String handler(SqlParam sqlParam, String columnName) {
            StringBuilder sqlBuilder = new StringBuilder();
            SqlPattern pattern = sqlParam.getPattern();
            Object paramValue = sqlParam.getParamValue();
            if (paramValue instanceof List) {
                List valueList = (List) paramValue;
                if (valueList.isEmpty()) {
                    return SQL_BLANK;
                }
                sqlBuilder.append(columnName).append(SQL_SPACE);
                sqlBuilder.append(pattern.getFlag()).append(SQL_SPACE).append(SQL_LEFT_BRACKET).append(SQL_SPACE);
                int size = valueList.size();
                for (int valueIndex = 0; valueIndex < size; valueIndex++) {
                    Object valueInList = valueList.get(valueIndex);
                    sqlBuilder.append(SQL_PLACE_HOLDER);
                    tuple.addValue(handlerParamValue(valueInList));
                    if (valueIndex != size - 1) {
                        sqlBuilder.append(SQL_COMMA);
                    }
                }
                sqlBuilder.append(SQL_RIGHT_BRACKET).append(SQL_SPACE);
            }
            return sqlBuilder.toString();
        }
    }

    /**
     * ?????? between ?????????pattern
     */
    class BetweenPatternHandler implements PatternHandler {

        @Override
        public boolean isSupport(SqlParam sqlParam) {
            SqlPattern pattern = sqlParam.getPattern();
            return pattern.equals(SqlPattern.between);
        }

        @Override
        public String handler(SqlParam sqlParam, String columnName) {
            StringBuilder sqlBuilder = new StringBuilder();
            SqlPattern pattern = sqlParam.getPattern();
            Object paramValue = sqlParam.getParamValue();
            if (paramValue instanceof List) {
                List valueList = (List) paramValue;
                requiredNotEmpty(valueList);
                if (valueList.size() != 2) {
                    return SQL_BLANK;
                }

                Object value1 = handlerParamValue(valueList.get(0));
                Object value2 = handlerParamValue(valueList.get(1));
                if (value1 == null || value2 == null) {
                    return SQL_BLANK;
                }
                tuple.addValue(value1);
                tuple.addValue(value2);
                sqlBuilder.append(columnName).append(SQL_SPACE);
                sqlBuilder.append(pattern.getFlag())
                    .append(SQL_SPACE)
                    .append(SQL_PLACE_HOLDER)
                    .append(SQL_SPACE)
                    .append(SQL_AND)
                    .append(SQL_SPACE)
                    .append(SQL_PLACE_HOLDER)
                    .append(SQL_SPACE);
            }
            return sqlBuilder.toString();
        }
    }


}
