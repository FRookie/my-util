package org.example.common.dao.impl;

import io.vertx.core.Handler;
import io.vertx.sqlclient.Tuple;
import org.example.common.dao.UserLogDao;
import org.example.common.model.UserLog;
import org.example.common.util.ModelUtils;
import org.example.common.util.db.MysqlManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * TD
 *
 * @date 2021/1/27
 */
public class UserLogDaoImpl extends BaseDaoImpl<UserLog> implements UserLogDao {

    private final Logger logger = LoggerFactory.getLogger(UserLogDaoImpl.class);

    /**
     * 统计用户活跃
     *
     * @param statSec 开始秒数
     * @param endSec  结束秒数
     */
    @Override
    public void selectStat(Handler<Integer> res, Integer statSec, Integer endSec) {
        String sql = "SELECT COUNT(DISTINCT userid) as activeCount from bw_user_log WHERE time >= ? and userid <> 0 and time "
            + "<?";
        Tuple tuple = Tuple.tuple().addInteger(statSec).addInteger(endSec);
        MysqlManager.getMySQLPool().preparedQuery(sql).execute(tuple, statRes -> {
            if (statRes.succeeded()) {
                Integer activeCount = ModelUtils.rowSetToInteger(statRes.result());
                res.handle(activeCount);
            } else {
                //null标识查询失败
                res.handle(null);
            }
        });
    }

    /**
     * 最近访问用户id 时间
     * @param time
     * @param res
     */
    @Override
    public void selectUserLastVisitTime(Integer time, Handler<List<UserLog>> res) {
        String sql = "SELECT max(time),userid,time FROM `bw_user_log` where userid > 0 and time > ? group by userid";
        Tuple tuple = Tuple.tuple().addInteger(time);
        MysqlManager.getMySQLPool().preparedQuery(sql).execute(tuple, result -> {
            if (result.succeeded()) {
                List<UserLog> userLogs = ModelUtils.rowSetToList(result.result(),UserLog.class);
                res.handle(userLogs);
            } else {
                //null标识查询失败
                res.handle(null);
            }
        });

    }
}
