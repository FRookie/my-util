package org.example.common.pojo;

import java.util.List;

/**
 * 连接查询结果封装
 *
 * @date 2020-12-31
 */
public class JoinResult<T,V> {

    T one;
    List<V> manyList;

    public T getOne() {
        return one;
    }

    public void setOne(T one) {
        this.one = one;
    }

    public List<V> getManyList() {
        return manyList;
    }

    public void setManyList(List<V> manyList) {
        this.manyList = manyList;
    }

}
