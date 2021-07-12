package org.example.common.dao;

import io.vertx.core.Handler;
import org.example.common.model.UserLog;

import java.util.List;

public interface UserLogDao extends BaseDao<UserLog> {

    /**
     * 统计用户活跃
     */
    void selectStat(Handler<Integer> res, Integer statSec, Integer endSec);


    /**
     * 最近访问用户id 时间
     * @param time
     * @param res
     */
    void selectUserLastVisitTime(Integer time, Handler<List<UserLog>> res);
}
