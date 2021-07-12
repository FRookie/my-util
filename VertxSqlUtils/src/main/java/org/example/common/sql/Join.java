package org.example.common.sql;

/**
 * @date 2020/12/29 16:43.
 */
public interface Join extends On {
    String LEFT_JOIN="left join";
    String INNER_JOIN="inner join";

    /**
     * 左连接
     * @param selectStatement
     * @return
     */
    On leftJoin(BaseStatement selectStatement);

    /**
     * 内连接
     * @param selectStatement
     * @return
     */
    On innerJoin(BaseStatement selectStatement);


}
