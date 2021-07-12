package org.example.common.dao;

import io.vertx.core.Handler;
import org.example.common.pojo.InsertResult;
import org.example.common.pojo.Page;
import org.example.common.pojo.SqlParam;
import org.example.common.sql.BaseStatement;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public interface BaseDao<T> {

    /**
     * <p>
     * 插入一条记录
     * </p>
     *
     * @param model 实体对象
     */
    void insert(T model, Handler<InsertResult> res);

    /**
     * <p>
     * 插入一条记录（选择字段， null 字段不插入）
     * </p>
     *
     * @param model 实体对象
     */
    void insertSelective(T model, Handler<InsertResult> res);


    /**
     * <p>
     * 批量插入
     * </p>
     *
     * @param entityList 实体对象列表
     */
    void insertBatch(List<T> entityList, Handler<InsertResult> res);

    /**
     * <p>
     * 批量插入（选择字段，null字段不插入）
     * </p>
     *
     * @param entityList 实体对象列表
     */
    void insertBatchSelective(List<T> entityList, Handler<InsertResult> res);

    /**
     * <p>
     * 根据 ID 删除
     * </p>
     *
     * @param id 主键ID
     */
    void deleteById(long id, Handler<Integer> res);

    /**
     * <p>
     * 根据 entity 条件，删除记录(entity中不为null的字段都会被加入where条件)
     * </p>
     *
     * @param entity 实体对象
     */
    void deleteSelective(T entity, Handler<Integer> res);

    /**
     * <p>
     * 删除（根据ID 批量删除）
     * </p>
     *
     * @param idList 主键ID列表
     */
    void deleteBatchIds(List<Long> idList, Handler<Integer> res);

    /**
     * <p>
     * 根据 ID 修改 (慎用)
     * </p>
     *
     * @param entity 实体对象
     * @return int
     */
    void updateById(T entity, Handler<Integer> res);

    /**
     * <p>
     * 根据 ID 选择修改(为null字段不修改)
     * </p>
     *
     * @param entity 实体对象
     */
    void updateSelectiveById(T entity, Handler<Integer> res);

    /**
     *
     * @param entity
     * @param filedCanNullList
     * @param res
     */
    void updateSelectiveById(T entity, List<String> filedCanNullList, Handler<Integer> res);

    /**
     * <p>
     * 根据 whereEntity 条件，更新记录
     * </p>
     *
     * @param entity      实体对象
     * @param whereEntity where实体对象
     */
    void update(T entity, T whereEntity, Handler<Integer> res);

    /**
     * <p>
     * 根据 whereEntity 条件，选择更新记录 (entity中null字段不更新)
     * </p>
     *
     * @param entity      实体对象
     * @param whereEntity where实体对象
     */
    void updateSelective(T entity, T whereEntity, Handler<Integer> res);

    /**
     * <p>
     * 根据ID 批量更新
     * </p>
     *
     * @param entityList 实体对象列表
     */
    void updateBatchSelectiveById(List<T> entityList, Handler<Integer> res);

    /**
     * <p>
     * 根据 ID 查询
     * </p>
     *
     * @param id 主键ID
     */
    void selectById(long id, Handler<T> res);


    /**
     * <p>
     * 查询（根据ID 批量查询）
     * </p>
     *
     * @param idList 主键ID列表
     */
    void selectBatchIds(List<? extends Long> idList, Handler<List<T>> res);

    /**
     * <p>
     * 查询多条记录
     * </p>
     *
     * @param entity 实体对象
     */
    void selectList(T entity, Handler<List<T>> res);

    /**
     * <p>
     * 查询多条记录(排序)
     * </p>
     *
     * @param entity 实体对象
     */
    void selectList(T entity, LinkedHashSet<SqlParam> sortSet, Handler<List<T>> res);

    /**
     * <p>
     * 查询多条记录
     * </p>
     *
     * @param entity 实体对象
     * @param isSort 是否排序
     * @param sortSet 排序参数集合
     */
    void selectList(T entity, boolean isSort, LinkedHashSet<SqlParam> sortSet, Handler<List<T>> res) throws IllegalAccessException;

    /**
     * <p>
     * 查询多条记录(排除字段+排序)
     * </p>
     * @param entity 实体类对象
     * @param exceptColumns 排除字段集合
     * @param sortSet 排序集合
     */
    void selectList(T entity, LinkedHashSet<String> exceptColumns, boolean isSort, LinkedHashSet<SqlParam> sortSet,
                    Handler<List<T>> res);

    /**
     * <p>
     * 查询多条记录(字段+排序)
     * </p>
     * @param entity 实体类对象
     * @param columns 字段集合
     * @param sortSet 排序集合
     * @param isSort 是否排序
     * @param res handler
     */
    void selectList(T entity, LinkedHashSet<String> columns, LinkedHashSet<SqlParam> sortSet, boolean isSort,
                    Handler<List<T>> res);
    /**
     * 分页查询
     *
     * @param entity 实体对象
     * @param page   分页对象
     * @param res    回调
     */
    void selectPage(T entity, Page page, Handler<Page> res);

    /**
     * 分页查询(排序)
     *
     * @param entity 实体对象
     * @param page   分页对象
     * @param res    回调
     */
    void selectPage(T entity, Page page, LinkedHashSet<SqlParam> sortSet, Handler<Page> res);

    /**
     * 分页查询
     *
     * @param entity        实体对象
     * @param page          分页对象
     * @param exceptColumns 不需要查询的字段
     * @param sortSet       排序参数集合
     */
    void selectSelectivePage(T entity, Page page, Collection<String> exceptColumns,
                             LinkedHashSet<SqlParam> sortSet,
                             Handler<Page> res);

    /**
     * 分页查询，复杂的sql查询直接传入已经处理过的BaseStatement
     *
     * @param selectStatement 生成sql的对象
     * @param page            page对象
     * @param res             处理回调
     */
    void selectSelectivePage(BaseStatement selectStatement, Page page, Handler<Page> res);

    /**
     * <p>
     * 根据 entity 条件，查询一条记录
     * </p>
     *
     * @param entity 实体对象
     */
    void selectOne(T entity, Handler<T> res);

    /**
     * <p>
     * 根据 entity 条件，查询一条记录
     * </p>
     *
     * @param entity 实体对象
     */
    void selectOne(T entity, Set<String> exceptColumn, Handler<T> res);

    /**
     * <p>
     * 根据 entity 条件，查询一条记录
     * </p>
     *
     * @param entity 实体对象
     */
    void selectSelectOne(T entity, Set<String> selectColumn, Handler<T> res);
    /**
     * <p>
     * 根据 entity 条件，查询总记录数
     * </p>
     *
     * @param entity 实体对象
     */
    void selectCount(T entity, Handler<Integer> res);



}
