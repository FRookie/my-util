package org.example.common.pojo;

import io.vertx.core.json.JsonObject;

import java.io.Serializable;
import java.util.List;

/**
 * 分页
 * @param <T>
 */
public class Page<T> implements Serializable {
    private static final long serialVersionUID = 5760097915453738435L;
    public static final int DEFAULT_PAGE_SIZE = 10;

    //总条数
    private Integer totalRows;
    //总页数
    private Integer totalPage;
    //当前页数
    private Integer page;
    //页面大小
    private Integer pageSize;
    //数据
    private List<T> rows;

    public Page() {
        this.page = 1;
        this.pageSize = DEFAULT_PAGE_SIZE;
    }

    public Page(Integer page, Integer pageSize) {
        this.page = (page == null || page <= 0) ? 1 : page;
        this.pageSize = (pageSize == null || pageSize <= 0) ? DEFAULT_PAGE_SIZE : pageSize;
    }

    public Page(Integer totalRows, Integer page, Integer pageSize, List<T> rows) {
        this.page = (page == null || page <= 0) ? 1 : page;
        this.totalRows = (totalRows == null || totalRows <= 0) ? 0 : totalRows;
        this.pageSize = (pageSize == null || pageSize <= 0) ? DEFAULT_PAGE_SIZE : pageSize;
        this.rows = rows;
        setTotalPage();
    }

    @Override
    public String toString() {
        return JsonObject.mapFrom(this).toString();
    }

    private void setTotalPage() {
        this.totalPage = totalRows % pageSize == 0 ? totalRows / pageSize : (totalRows / pageSize) + 1;
    }

    public Integer getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Integer totalRows) {
        this.totalRows = totalRows;
        setTotalPage();
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public Integer getPage() {
        return page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }
}
