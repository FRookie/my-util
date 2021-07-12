package org.example.common.enums;

/**
 * 匹配模式
 *
 * @date 2020/12/21 17:46.
 */
public enum SqlPattern {
    eq("=","等于"),
    ne("<>","不等于"),
    eq_null("<=>","等于,包括null值"),
    lt("<","小于"),
    le("<=","小于等于"),
    gt(">","大于"),
    ge(">=","大于等于"),
    in("in ", "in"),
    Like("like", "like"),
    RLike("like", "like ?%"),
    LLike("like","like %?"),
    between("between","在......之间"),
    and("and","且"),
    or("or","或"),
    limit("limit", "分页查询"),
    order("order by", "排序"),
    ;
    private final String flag;
    private final String description;

    SqlPattern(String flag, String description) {
        this.flag = flag;
        this.description =description;
    }

    public String getFlag() {
        return flag;
    }

    public String getDescription() {
        return description;
    }
}
