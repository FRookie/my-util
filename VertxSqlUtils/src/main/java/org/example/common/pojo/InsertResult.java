package org.example.common.pojo;

/**
 * 数据库insert返回结果封装
 * @date 2020-12-29
 */
public class InsertResult {

    //插入行数
    private Integer rowCount;
    //主键返回
    private Long id;

    public InsertResult(Integer rowCount, Long id) {
        this.rowCount = rowCount;
        this.id = id;
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
