package org.example.common.dao.impl;

import io.vertx.core.Handler;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;
import org.example.common.dao.BaseDao;
import org.example.common.exception.IAException;
import org.example.common.pojo.InsertResult;
import org.example.common.pojo.Page;
import org.example.common.pojo.SqlParam;
import org.example.common.sql.*;
import org.example.common.util.ModelUtils;
import org.example.common.util.db.MysqlManager;
import org.example.common.util.db.SqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @date 2020-12-22
 */
public abstract class BaseDaoImpl<T> implements BaseDao<T> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * <p>
     * 插入一条记录
     * </p>
     *
     * @param model 实体对象
     */
    @Override
    public void insert(T model, Handler<InsertResult> res){
        if (model == null) {
            res.handle(null);
            return;
        }
        MySQLPool mySQLPool = MysqlManager.getMySQLPool();

        List<T> list = new ArrayList<>();
        list.add(model);
        Map<String, Object> map = InsertGenerator.generateInsertBatchSql(list, false);
        String sql = (String) map.get("sql");
        List<Object> tupleList = (List<Object>) map.get("tuple");

        mySQLPool.preparedQuery(sql).execute(Tuple.tuple(tupleList), ar -> {
            if (ar.succeeded()) {
                RowSet<Row> rowSet = ar.result();
                res.handle(new InsertResult(rowSet.rowCount(), rowSet.property(MySQLClient.LAST_INSERTED_ID)));
            } else {
                logger.error("failed insert " + model.getClass().getName());
                logger.error(ar.cause().getMessage());
                res.handle(null);
            }
        });
    }

    /**
     * <p>
     * 插入一条记录（选择字段， null 字段不插入）
     * </p>
     *
     * @param model 实体对象
     */
    @Override
    public void insertSelective(T model, Handler<InsertResult> res){
        if (model == null) {
            res.handle(null);
            return;
        }
        MySQLPool mySQLPool = MysqlManager.getMySQLPool();

        List<T> list = new ArrayList<>();
        list.add(model);
        Map<String, Object> map = InsertGenerator.generateInsertBatchSql(list, true);
        String sql = (String) map.get("sql");
        List<Object> tupleList = (List<Object>) map.get("tuple");

        mySQLPool.preparedQuery(sql).execute(Tuple.tuple(tupleList), ar -> {
            if (ar.succeeded()) {
                RowSet<Row> rowSet = ar.result();
                res.handle(new InsertResult(rowSet.rowCount(), rowSet.property(MySQLClient.LAST_INSERTED_ID)));
            } else {
                logger.error("failed insertSelective " + model.getClass().getName());
                logger.error("detail error:", ar.cause());
                res.handle(null);
            }
        });
    }


    /**
     * <p>
     * 批量插入
     * </p>
     *
     * @param entityList 实体对象列表
     */
    @Override
    public void insertBatch(List<T> entityList, Handler<InsertResult> res) {
        if (entityList == null || entityList.size() == 0) {
            res.handle(null);
            return;
        }

        MySQLPool mySQLPool = MysqlManager.getMySQLPool();

        Map<String, Object> map = InsertGenerator.generateInsertBatchSql(entityList, false);
        String sql = (String) map.get("sql");
        List<Object> tupleList = (List<Object>) map.get("tuple");

        mySQLPool.preparedQuery(sql).execute(Tuple.tuple(tupleList), ar -> {
            if (ar.succeeded()) {
                RowSet<Row> rowSet = ar.result();
                res.handle(new InsertResult(rowSet.rowCount(), rowSet.property(MySQLClient.LAST_INSERTED_ID)));
            } else {
                logger.error("failed insertBatch" + entityList.get(0).getClass().getName());
                logger.error(ar.cause().getMessage());
                res.handle(null);
            }
        });
    }

    /**
     * <p>
     * 批量插入（选择字段，null字段不插入）
     * </p>
     *
     * @param entityList 实体对象列表
     */
    @Override
    public void insertBatchSelective(List<T> entityList, Handler<InsertResult> res){
        if (entityList == null || entityList.size() == 0) {
            res.handle(null);
            return;
        }

        MySQLPool mySQLPool = MysqlManager.getMySQLPool();
        Map<String, Object> map = InsertGenerator.generateInsertBatchSql(entityList, true);
        String sql = (String) map.get("sql");
        List<Object> tupleList = (List<Object>) map.get("tuple");

        mySQLPool.preparedQuery(sql).execute(Tuple.tuple(tupleList), ar -> {
            if (ar.succeeded()) {
                RowSet<Row> rowSet = ar.result();
                res.handle(new InsertResult(rowSet.rowCount(), rowSet.property(MySQLClient.LAST_INSERTED_ID)));
            } else {
                logger.error("failed insertBatchSelective,cause by ->{}" ,ar.cause().getMessage());
                res.handle(null);
            }
        });
    }


    /**
     * <p>
     * 根据 ID 删除
     * </p>
     *
     * @param id 主键ID
     */
    @Override
    public void deleteById(long id, Handler<Integer> res) {
        MySQLPool mySQLPool = MysqlManager.getMySQLPool();

        Class clazz = getTClass();

        Set<SqlParam> paramSet = new HashSet<>();
        paramSet.add(new SqlParam("id", id));
        BaseStatement deleteStatement = new DeleteStatement(clazz);
        String sql = deleteStatement.eq("id")
            .params(paramSet)
            .build();
        mySQLPool.preparedQuery(sql).execute(deleteStatement.getTuple(), ar -> {
            if (ar.succeeded()) {
                res.handle(ar.result().rowCount());
            } else {
                logger.error("failed delete" + clazz.getName());
                logger.error("Cause by -> {}",ar.cause());
                res.handle(0);
            }
        });
    }


    /**
     * <p>
     * 根据 entity 条件，删除记录(entity中不为null的字段都会被加入where条件)
     * </p>
     *
     * @param entity 实体对象
     */
    @Override
    public void deleteSelective(T entity, Handler<Integer> res) {
        if (entity == null) {
            res.handle(0);
            return;
        }

        MySQLPool mySQLPool = MysqlManager.getMySQLPool();

        BaseStatement deleteStatement = new DeleteStatement(entity.getClass());
        Set<SqlParam> paramSet = new HashSet<>();

        Class clazz = entity.getClass();
        Field[] fieldArr = clazz.getDeclaredFields();
        try {
            for (Field field : fieldArr) {
                field.setAccessible(true);
                if ("serialVersionUID".equals(field.getName())) {
                    continue;
                }

                Object value = field.get(entity);

                if (value == null) {
                    continue;
                }
                deleteStatement.eq(field.getName());
                paramSet.add(new SqlParam(field.getName(), value));
            }
        } catch (IllegalAccessException e) {
            throw new IAException(e.getMessage());
        }

        String sql = deleteStatement.params(paramSet).build();
        Tuple tuple = deleteStatement.getTuple();
        mySQLPool.preparedQuery(sql).execute(tuple, ar -> {
            if (ar.succeeded()) {
                res.handle(ar.result().rowCount());
            } else {
                logger.error("failed delete" + entity.getClass().getName());
                logger.error(ar.cause().getMessage());
                res.handle(0);
            }
        });
    }


    /**
     * <p>
     * 删除（根据ID 批量删除）
     * </p>
     *
     * @param idList 主键ID列表
     */
    @Override
    public void deleteBatchIds(List<Long> idList, Handler<Integer> res) {
        if (idList == null || idList.size() == 0) {
            res.handle(0);
            return;
        }
        MySQLPool mySQLPool = MysqlManager.getMySQLPool();

        Class<T> clazz = getTClass();

        BaseStatement deleteStatement = new DeleteStatement(clazz);
        String sql = deleteStatement.in("id").params(new SqlParam("id", idList)).build();

        mySQLPool.preparedQuery(sql).execute(deleteStatement.getTuple(), ar -> {
            if (ar.succeeded()) {
                res.handle(ar.result().rowCount());
            } else {
                logger.error("failed deleteBatch" + clazz.getName());
                logger.error(ar.cause().getMessage());
                res.handle(0);
            }
        });
    }


    /**
     * <p>
     * 根据 ID 修改 (慎用)
     * </p>
     *
     * @param entity 实体对象
     * @return int
     */
    @Override
    public void updateById(T entity, Handler<Integer> res) {
        if (entity == null) {
            res.handle(0);
            return;
        }

        MySQLPool mySQLPool = MysqlManager.getMySQLPool();

        Class clazz = entity.getClass();
        BaseStatement updateStatement = new UpdateStatement(clazz);
        Set<SqlParam> paramSet = new HashSet<>();

        Field[] fieldArr = clazz.getDeclaredFields();
        try {
            for (Field field : fieldArr) {
                field.setAccessible(true);
                if ("serialVersionUID".equals(field.getName())) {
                    continue;
                }
                Object value = field.get(entity);
                paramSet.add(new SqlParam(field.getName(), value));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        String sql = updateStatement.columnSetExcept(new HashSet<String>())
            .eq("id")
            .params(paramSet)
            .build();

        mySQLPool.preparedQuery(sql).execute(updateStatement.getTuple(), ar -> {
            if (ar.succeeded()) {
                res.handle(ar.result().rowCount());
            } else {
                logger.error("failed updateById " + clazz.getName());
                logger.error(ar.cause().getMessage());
                res.handle(0);
            }
        });
    }


    /**
     * <p>
     * 根据 ID 选择修改(为null字段不修改)
     * </p>
     *
     * @param entity 实体对象
     */
    @Override
    public void updateSelectiveById(T entity, Handler<Integer> res) {
        updateSelectiveById(entity, Collections.emptyList(), res);
    }

    @Override
    public void updateSelectiveById(T entity, List<String> filedCanNullList, Handler<Integer> res) {
        if (entity == null) {
            res.handle(0);
            return;
        }

        Class clazz = entity.getClass();
        BaseStatement updateStatement = new UpdateStatement(clazz);
        Set<SqlParam> paramSet = new HashSet<>();

        setParamCollection(paramSet, entity, filedCanNullList);

        String sql = updateStatement.columnSetExcept(new HashSet<>())
            .eq("id")
            .params(paramSet)
            .build();

        MySQLPool mySQLPool = MysqlManager.getMySQLPool();
        mySQLPool.preparedQuery(sql).execute(updateStatement.getTuple(), ar -> {
            if (ar.succeeded()) {
                res.handle(ar.result().rowCount());
            } else {
                logger.error("failed updateSelectiveById " + clazz.getName());
                logger.error("Cause by -> ", ar.cause());
                res.handle(0);
            }
        });
    }

    /**
     * <p>
     * 根据 whereEntity 条件，更新记录
     * </p>
     *
     * @param entity      实体对象
     * @param whereEntity where实体对象
     */
    @Override
    public void update(T entity, T whereEntity, Handler<Integer> res) {
        if (entity == null || whereEntity == null) {
            res.handle(0);
            return;
        }

        Class clazz = entity.getClass();
        Set<SqlParam> paramSet = new HashSet<>();
        BaseStatement updateStatement = new UpdateStatement(clazz);

        Field[] entityFieldArr = entity.getClass().getDeclaredFields();
        try {
            for (Field field : entityFieldArr) {
                field.setAccessible(true);
                if ("serialVersionUID".equals(field.getName())) {
                    continue;
                }
                Object value = field.get(entity);
                paramSet.add(new SqlParam(field.getName(), value));
            }
        } catch (IllegalAccessException e) {
            throw new IAException(e.getMessage());
        }

        setWhereConditionAndParamCollection(whereEntity, updateStatement, paramSet);

        String sql = updateStatement.columnSetExcept(new HashSet<String>())
            .params(paramSet)
            .build();

        MySQLPool mySQLPool = MysqlManager.getMySQLPool();
        mySQLPool.preparedQuery(sql).execute(updateStatement.getTuple(), ar -> {
            if (ar.succeeded()) {
                res.handle(ar.result().rowCount());
            } else {
                logger.error("failed updateByWhereEntity " + clazz.getName());
                logger.error(ar.cause().getMessage());
                res.handle(0);
            }
        });
    }


    /**
     * <p>
     * 根据 whereEntity 条件，选择更新记录 (entity中null字段不更新)
     * </p>
     *
     * @param entity      实体对象
     * @param whereEntity where实体对象
     */
    @Override
    public void updateSelective(T entity, T whereEntity, Handler<Integer> res) {
        if (entity == null || whereEntity == null) {
            res.handle(0);
            return;
        }

        Class clazz = entity.getClass();
        BaseStatement updateStatement = new UpdateStatement(clazz);
        Set<SqlParam> paramList = new HashSet<>();

        setWhereConditionAndParamCollection(whereEntity, updateStatement, paramList);

        setParamCollection(paramList, entity, new ArrayList<String>());

        String sql = updateStatement.columnSetExcept(new HashSet<String>())
            .params(paramList)
            .build();

        MySQLPool mySQLPool = MysqlManager.getMySQLPool();
        mySQLPool.preparedQuery(sql).execute(updateStatement.getTuple(), ar -> {
            if (ar.succeeded()) {
                res.handle(ar.result().rowCount());
            } else {
                logger.error("failed updateSelectiveByWhereEntity " + clazz.getName());
                logger.error(ar.cause().getMessage());
                res.handle(0);
            }
        });
    }
    /**
     * <p>
     * 根据ID 批量更新
     * </p>
     *
     * @param entityList 实体对象列表
     */
    @Override
    public void updateBatchSelectiveById(List<T> entityList, Handler<Integer> res) {
        if (entityList == null || entityList.size() == 0) {
            res.handle(0);
            return;
        }
        MySQLPool mySQLPool = MysqlManager.getMySQLPool();

        mySQLPool.getConnection(ar -> {
            if (ar.succeeded()) {
                SqlConnection sqlConnection = ar.result();
                //开启事务
                Transaction transaction = sqlConnection.begin();
                executeUpdateBatchTransaction(sqlConnection,transaction,entityList,0,updateCount -> res.handle(updateCount));
            } else {
                res.handle(0);
                logger.error("failed to get SqlConnection. " + ar.cause().getMessage());
            }
        });
    }

    /**
     * 批量更新事务操作
     * @param entityList 实体对象列表
     * @param index 当前下标
     * @param res 回调
     */
    private void executeUpdateBatchTransaction(SqlConnection connection,
                                               Transaction transaction,
                                               List<T> entityList,
                                               int index,
                                               Handler<Integer> res){
        T entity = entityList.get(index);
        BaseStatement updateStatement = new UpdateStatement(entity.getClass());
        Set<SqlParam> paramSet = new HashSet<>();
        setParamCollection(paramSet, entity, new ArrayList<String>());
        String sql = updateStatement.columnSetExcept(new HashSet<>())
            .eq("id")
            .params(paramSet)
            .build();

        connection.preparedQuery(sql).execute(updateStatement.getTuple(), asyncResult -> {
            if(asyncResult.succeeded()){
                if(index == entityList.size() - 1){
                    transaction.commit(result -> {
                        if(result.failed()){
                            res.handle(0);
                            logger.error("failed to updateBatchSelectiveById. Cause by -> {}", result.cause().getMessage());
                        }else{
                            res.handle(entityList.size());
                        }
                        connection.close();
                    });
                }else{
                    executeUpdateBatchTransaction(connection, transaction, entityList, index + 1, c -> res.handle(c));
                }
            }else{
                res.handle(0);
                connection.close();
            }
        });
    }

    /**
     * <p>
     * 根据 ID 查询
     * </p>
     *
     * @param id 主键ID
     */
    @Override
    public void selectById(long id, Handler<T> res) {
        Class<T> clazz = getTClass();
        BaseStatement selectStatement = new SelectStatement(clazz);
        String sql = selectStatement.columnSetExcept(new HashSet<String>())
            .eq("id")
            .params(new SqlParam("id", id))
            .build();
        Tuple tuple = selectStatement.getTuple();

        MySQLPool mySQLPool = MysqlManager.getMySQLPool();
        mySQLPool.preparedQuery(sql).execute(tuple, ar -> {
            if (ar.succeeded()) {
                res.handle(ModelUtils.rowSetToModel(ar.result(), clazz));
            } else {
                logger.error("failed selectById " + clazz.getName());
                logger.error(ar.cause().getMessage());
                res.handle(null);
            }
        });
    }


    /**
     * <p>
     * 查询（根据ID 批量查询）
     * </p>
     *
     * @param idList 主键ID列表
     */
    @Override
    public void selectBatchIds(List<? extends Long> idList, Handler<List<T>> res) {
        Class<T> clazz = getTClass();
        BaseStatement selectStatement = new SelectStatement(clazz);
        String sql = selectStatement.columnSetExcept(new HashSet<String>())
            .in("id")
            .params(new SqlParam("id", idList))
            .build();
        Tuple tuple = selectStatement.getTuple();

        MySQLPool mySQLPool = MysqlManager.getMySQLPool();
        mySQLPool.preparedQuery(sql).execute(tuple, ar -> {
            if (ar.succeeded()) {
                res.handle(ModelUtils.rowSetToList(ar.result(), clazz));
            } else {
                logger.error("failed selectBatchIds " + clazz.getName());
                logger.error(ar.cause().getMessage());
                res.handle(null);
            }
        });
    }


    /**
     * <p>
     * 查询多条记录
     * </p>
     *
     * @param entity 实体对象
     */
    @Override
    public void selectList(T entity, Handler<List<T>> res) {
        selectList(entity, false, null, res);
    }


    /**
     * <p>
     * 查询多条记录(排序)
     * </p>
     *
     * @param entity 实体对象
     */
    @Override
    public void selectList(T entity, LinkedHashSet<SqlParam> sortSet, Handler<List<T>> res) {
        Objects.requireNonNull(sortSet);
        selectList(entity, true, sortSet, res);
    }


    /**
     * 查询多条记录
     *
     * @param entity  实体对象
     * @param isSort  是否排序
     * @param sortSet 排序参数集合
     * @param res     回调
     */
    @Override
    public void selectList(T entity, boolean isSort, LinkedHashSet<SqlParam> sortSet, Handler<List<T>> res) {
        selectList(entity, new LinkedHashSet<>(), isSort, sortSet, res);
    }


    /**
     * 查询多条记录
     *
     * @param entity  实体对象
     * @param isSort  是否排序
     * @param sortSet 排序参数集合
     * @param res     回调
     */
    @Override
    public void selectList(T entity, LinkedHashSet<String> exceptColumns, boolean isSort, LinkedHashSet<SqlParam> sortSet,
                           Handler<List<T>> res) {
        Objects.requireNonNull(entity);

        Class clazz = entity.getClass();
        Set<SqlParam> paramSet = new HashSet<>();
        BaseStatement selectStatement = new SelectStatement(clazz).columnSetExcept(exceptColumns);

        setConditionAndParamCollection(paramSet, entity, selectStatement);

        String sql = "";
        if (isSort) {
            sql = selectStatement.params(paramSet).orderByParams(sortSet).build();
        } else {
            sql = selectStatement.params(paramSet).build();
        }
        Tuple tuple = selectStatement.getTuple();

        MySQLPool mySQLPool = MysqlManager.getMySQLPool();
        mySQLPool.preparedQuery(sql).execute(tuple, ar -> {
            if (ar.succeeded()) {
                res.handle(ModelUtils.rowSetToList(ar.result(), clazz));
            } else {
                logger.error("failed selectList, entity:[{}]", entity, ar.cause());
                res.handle(null);
            }
        });
    }

    /**
     * 查询多条记录
     *
     * @param entity  实体对象
     * @param isSort  是否排序
     * @param sortSet 排序参数集合
     * @param res     回调
     */
    @Override
    public void selectList(T entity, LinkedHashSet<String> columns, LinkedHashSet<SqlParam> sortSet, boolean isSort,
                           Handler<List<T>> res) {
        Objects.requireNonNull(entity);

        Class clazz = entity.getClass();
        Set<SqlParam> paramSet = new HashSet<>();
        BaseStatement selectStatement = new SelectStatement(clazz).columnSet(columns);

        setConditionAndParamCollection(paramSet, entity, selectStatement);

        String sql = "";
        if (isSort) {
            sql = selectStatement.params(paramSet).orderByParams(sortSet).build();
        } else {
            sql = selectStatement.params(paramSet).build();
        }
        Tuple tuple = selectStatement.getTuple();

        MySQLPool mySQLPool = MysqlManager.getMySQLPool();
        mySQLPool.preparedQuery(sql).execute(tuple, ar -> {
            if (ar.succeeded()) {
                res.handle(ModelUtils.rowSetToList(ar.result(), clazz));
            } else {
                logger.error("failed selectList " + clazz.getName());
                logger.error("Cause by -> {}",ar.cause());
                res.handle(null);
            }
        });
    }


    /**
     * 分页查询（不排序）
     *
     * @param entity 实体对象
     * @param page   分页对象
     * @param res    回调
     */
    @Override
    public void selectPage(T entity, Page page, Handler<Page> res) {
        selectPage(entity, page, false, null, res);
    }


    /**
     * 分页查询(排序)
     *
     * @param entity 实体对象
     * @param page   分页对象
     * @param res    回调
     */
    @Override
    public void selectPage(T entity, Page page, LinkedHashSet<SqlParam> sortSet, Handler<Page> res) {
        Objects.requireNonNull(sortSet);
        selectPage(entity, page, true, sortSet, res);
    }

    /**
     * 分页查询
     *
     * @param entity  实体对象
     * @param page    分页对象
     * @param isSort  是否排序
     * @param sortSet 排序参数集合
     */
    private void selectPage(T entity, Page page, boolean isSort, LinkedHashSet<SqlParam> sortSet, Handler<Page> res) {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(page);
        MySQLPool mySQLPool = MysqlManager.getMySQLPool();

        Class clazz = entity.getClass();
        Set<SqlParam> paramSet = new HashSet<>();
        BaseStatement selectStatement = new SelectStatement(clazz).columnSetExcept(new HashSet<String>());

        int pageNum = page.getPage();
        int pageSize = page.getPageSize();

        setConditionAndParamCollection(paramSet, entity, selectStatement);

        String countSql = selectStatement.params(paramSet).buildPageCountSql();
        Tuple countTuple = selectStatement.getTuple();
        mySQLPool.preparedQuery(countSql).execute(countTuple, rowSetAsyncResult -> {
            if (rowSetAsyncResult.succeeded()) {
                int totalCount = rowSetAsyncResult.result().iterator().next().get(Integer.class, 0);

                String sql = "";
                if (isSort) {
                    sql = selectStatement.page(pageNum, pageSize, totalCount)
                        .orderByParams(sortSet)
                        .build();
                } else {
                    sql = selectStatement.page(pageNum, pageSize, totalCount).build();
                }
                Tuple tuple = selectStatement.getTuple();
                mySQLPool.preparedQuery(sql).execute(tuple, ar -> {
                    if (ar.succeeded()) {
                        page.setTotalRows(totalCount);
                        page.setRows(ModelUtils.rowSetToList(ar.result(), clazz));
                    } else {
                        logger.error("failed selectPage " + clazz.getName());
                        logger.error(ar.cause().getMessage());
                    }
                    res.handle(page);
                });
            } else {
                res.handle(page);
            }
        });
    }

    /**
     * 分页查询
     *
     * @param entity        实体对象
     * @param page          分页对象
     * @param exceptColumns 不需要查询的字段
     * @param sortSet       排序参数集合
     */
    @Override
    public void selectSelectivePage(T entity, Page page, Collection<String> exceptColumns,
                                    LinkedHashSet<SqlParam> sortSet,
                                    Handler<Page> res) {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(page);
        Class clazz = entity.getClass();
        BaseStatement selectStatement = new SelectStatement(clazz).columnSetExcept(exceptColumns)
            .orderByParams(sortSet == null ? new LinkedHashSet<>() : sortSet);
        Set<SqlParam> paramSet = new HashSet<>();
        setConditionAndParamCollection(paramSet, entity, selectStatement);
        selectStatement.params(paramSet);
        selectSelectivePage(selectStatement, page, res);
    }

    /**
     * 分页查询,测试中
     *
     * @param selectStatement 生成sql的对象
     * @param page            page对象
     * @param res             处理回调
     */
    @Override
    public void selectSelectivePage(BaseStatement selectStatement, Page page, Handler<Page> res) {
        Objects.requireNonNull(page);
        Objects.requireNonNull(selectStatement);
        MySQLPool mySQLPool = MysqlManager.getMySQLPool();

        Class clazz = selectStatement.getClz();
        int pageNum = page.getPage();
        int pageSize = page.getPageSize();

        String countSql = selectStatement.buildPageCountSql();
        Tuple countTuple = selectStatement.getTuple();
        mySQLPool.preparedQuery(countSql).execute(countTuple, rowSetAsyncResult -> {
            if (rowSetAsyncResult.succeeded()) {
                int totalCount = rowSetAsyncResult.result().iterator().next().get(Integer.class, 0);
                String sql = selectStatement.page(pageNum, pageSize, totalCount).build();
                Tuple tuple = selectStatement.getTuple();
                mySQLPool.preparedQuery(sql).execute(tuple, ar -> {
                    if (ar.succeeded()) {
                        page.setTotalRows(totalCount);
                        page.setRows(ModelUtils.rowSetToList(ar.result(), clazz));
                    } else {
                        logger.error("failed selectPage " + clazz.getName());
                        logger.error(ar.cause().getMessage());
                    }
                    res.handle(page);
                });
            } else {
                res.handle(page);
            }
        });
    }

    /**
     * <p>
     * 根据 entity 条件，查询一条记录
     * </p>
     *
     * @param entity 实体对象
     */
    @Override
    public void selectOne(T entity, Handler<T> res) {
        selectOne(entity,new HashSet<>(),res);
    }

    /**
     * <p>
     * 根据 entity 条件，查询一条记录
     * </p>
     *
     * @param entity 实体对象
     */
    @Override
    public void selectOne(T entity, Set<String> exceptColumn, Handler<T> res) {
        Objects.requireNonNull(exceptColumn);
        Class clazz = entity.getClass();
        BaseStatement selectStatement = new SelectStatement(clazz)
            .columnSetExcept(exceptColumn)
            .page(1, 1, 1);
        Set<SqlParam> paramSet = new HashSet<>();

        setConditionAndParamCollection(paramSet, entity, selectStatement);

        String sql = selectStatement.params(paramSet).build();
        Tuple tuple = selectStatement.getTuple();

        MySQLPool mySQLPool = MysqlManager.getMySQLPool();
        mySQLPool.preparedQuery(sql).execute(tuple, ar -> {
            if (ar.succeeded()) {
                res.handle(ModelUtils.rowSetToModel(ar.result(), getTClass()));
            } else {
                logger.error("failed selectOne " + clazz.getName());
                logger.error(ar.cause().getMessage());
                res.handle(null);
            }
        });
    }

    /**
     * <p>
     * 根据 entity 条件，查询一条记录
     * </p>
     *
     * @param entity 实体对象
     */
    @Override
    public void selectSelectOne(T entity, Set<String> selectColumn, Handler<T> res) {
        Objects.requireNonNull(selectColumn);
        Class clazz = entity.getClass();
        BaseStatement selectStatement = new SelectStatement(clazz)
            .columnSet(selectColumn)
            .page(1, 1, 1);
        Set<SqlParam> paramSet = new HashSet<>();

        setConditionAndParamCollection(paramSet, entity, selectStatement);

        String sql = selectStatement.params(paramSet).build();
        Tuple tuple = selectStatement.getTuple();

        MySQLPool mySQLPool = MysqlManager.getMySQLPool();
        mySQLPool.preparedQuery(sql).execute(tuple, ar -> {
            if (ar.succeeded()) {
                res.handle(ModelUtils.rowSetToModel(ar.result(), getTClass()));
            } else {
                logger.error("failed selectOne " + clazz.getName());
                logger.error(ar.cause().getMessage());
                res.handle(null);
            }
        });
    }

    /**
     * <p>
     * 根据 entity 条件，查询总记录数
     * </p>
     *
     * @param entity 实体对象
     */
    @Override
    public void selectCount(T entity, Handler<Integer> res) {
        Objects.requireNonNull(entity);
        Class clazz = entity.getClass();
        BaseStatement selectStatement = new SelectStatement(clazz);
        Set<SqlParam> paramSet = new HashSet<>();

        setConditionAndParamCollection(paramSet, entity, selectStatement);

        String sql = selectStatement.columnSet("COUNT(id) as c,id").params(paramSet).build();
        Tuple tuple = selectStatement.getTuple();

        MySQLPool mySQLPool = MysqlManager.getMySQLPool();
        mySQLPool.preparedQuery(sql).execute(tuple, ar -> {
            if (ar.succeeded()) {
                res.handle(ModelUtils.rowSetToInteger(ar.result()));
            } else {
                logger.error("failed selectCount " + clazz.getName());
                logger.error(ar.cause().getMessage());
                res.handle(0);
            }
        });
    }

    /**
     * 构建SqlParam集合
     *
     * @param paramSet SqlParam集合
     * @param entity   实体对象
     * @param filedCanNullList
     */
    protected <R> void setParamCollection(Set<SqlParam> paramSet, R entity, List<String> filedCanNullList) {
        Field[] fieldArr = entity.getClass().getDeclaredFields();
        try {
            for (Field field : fieldArr) {
                field.setAccessible(true);
                if ("serialVersionUID".equals(field.getName())) {
                    continue;
                }
                Object value = field.get(entity);

                if (value == null && !filedCanNullList.contains(field.getName())) {
                    continue;
                }
                paramSet.add(new SqlParam(field.getName(), value));
            }
        } catch (IllegalAccessException e) {
            throw new IAException(e.getMessage());
        }
    }

    /**
     * 构建where条件以及SqlParam集合
     *
     * @param paramSet SqlParam集合
     * @param entity   实体对象
     */
    protected <R> void setConditionAndParamCollection(Set<SqlParam> paramSet, R entity, BaseStatement baseStatement) {
        Field[] fieldArr = entity.getClass().getDeclaredFields();
        try {
            for (Field field : fieldArr) {
                field.setAccessible(true);
                if ("serialVersionUID".equals(field.getName()) || field.get(entity) == null) {
                    continue;
                }
                baseStatement.eq(field.getName());
                paramSet.add(new SqlParam(field.getName(), field.get(entity)));
            }
        } catch (IllegalAccessException e) {
            throw new IAException(e.getMessage());
        }
    }

    protected <R> void setWhereConditionAndParamCollection(R whereEntity, BaseStatement updateStatement,
                                                      Set<SqlParam> paramList) {
        try {
            Field[] fieldArr = whereEntity.getClass().getDeclaredFields();
            for (Field field : fieldArr) {
                field.setAccessible(true);
                String fieldName = field.getName();

                if ("serialVersionUID".equals(fieldName) || field.get(whereEntity) == null) {
                    continue;
                }

                String columnName = SqlUtils.getColumnByFieldName(whereEntity.getClass(),fieldName);
                updateStatement.eq(fieldName + "_where",columnName);
                paramList.add(new SqlParam(fieldName + "_where", field.get(whereEntity)));
            }
        } catch (IllegalAccessException e) {
            throw new IAException(e.getMessage());
        }
    }

    private Class<T> getTClass() {
        Type type = getClass().getGenericSuperclass();
        Type trueType = ((ParameterizedType) type).getActualTypeArguments()[0];
        return (Class<T>) trueType;
    }

}
