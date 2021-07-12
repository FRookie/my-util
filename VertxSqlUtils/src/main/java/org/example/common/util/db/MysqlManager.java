package org.example.common.util.db;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * mysql连接工具类
 * @date 2020-12-22
 */
public class MysqlManager {

    private volatile static MysqlManager INSTANCE = null;
    private static MySQLPool mySQLPool;
    private static final Logger logger = LoggerFactory.getLogger(MysqlManager.class);

    private MysqlManager() {

//        JsonObject config = ;
//        MySQLConnectOptions connectOptions = new MySQLConnectOptions()
//            .setPort(config.getInteger(ConfigKey.DB.PORT, 3306))
//            .setHost(config.getString(ConfigKey.DB.HOST, "127.0.0.1"))
//            .setDatabase(config.getString(ConfigKey.DB.DATABASE))
//            .setUser(config.getString(ConfigKey.DB.USER, "root"))
//            .setPassword(config.getString(ConfigKey.DB.PASSWORD, "123456"))
//            .setIdleTimeout(config.getInteger(ConfigKey.DB.MAX_IDLE_TIME, 10));
//
//        PoolOptions poolOptions = new PoolOptions()
//                .setMaxSize(config.getInteger(ConfigKey.DB.MAX_POOL_SIZE, 10))
//                .setMaxWaitQueueSize(config.getInteger(ConfigKey.DB.MAX_WAIT_QUEUE_SIZE, 1000));
//        mySQLPool = MySQLPool.pool(Vertx.currentContext().owner(), connectOptions, poolOptions);
    }

    public static Future<MysqlManager> init() {
        Promise<MysqlManager> promise = Promise.promise();
        if (INSTANCE != null) {
            throw new RuntimeException("MysqlManager initialization completed");
        }
        INSTANCE = new MysqlManager();
        checkConn(promise);
        return promise.future();
    }

    /**
     * 验证能争取获取连接
     */
    private static void checkConn(Promise<MysqlManager> promise) {
        MysqlManager.getMySQLPool().getConnection(conRes -> {
            if (conRes.succeeded()) {
                logger.info("mysql connection pool initialization success");
                //关闭连接
                conRes.result().close();
                promise.complete(INSTANCE);
            } else {
                logger.error("mysql connection pool initialization fail!Cause by ->{}",conRes.cause().getMessage());
                promise.fail(conRes.cause());
            }
        });
    }

    /**
     * 返回单例
     */
    public static MysqlManager getInstance() {
        if (INSTANCE == null) {
            throw new RuntimeException("MysqlManager uninitialized");
        }
        return INSTANCE;
    }

    /**
     * 返回连接池
     */
    public static MySQLPool getMySQLPool() {
        if (mySQLPool != null) {
            return mySQLPool;
        }
        throw new RuntimeException("MySQLPool Incomplete initialization");
    }

}
