package org.example.common.model;

import org.example.common.annotation.Column;
import org.example.common.annotation.Table;

import java.io.Serializable;

/**
 * api访问日志表
 * @date 2021-02-05
 */
@Table("bw_user_log")
public class UserLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column("id")
    private Integer id;

    /**
     * 路由
     */
    @Column("route")
    private String route;

    /**
     * 参数
     */
    @Column("parameters")
    private String parameters;

    /**
     * 访问ip
     */
    @Column("ip")
    private String ip;

    /**
     * 用户id（没有存0）
     */
    @Column("userid")
    private Integer userid;

    /**
     * 访问时间
     */
    @Column("time")
    private Integer time;

    /**
     * 设备类型（1安卓，2IOS，3WEB，4WAP）
     */
    @Column("device")
    private Integer device;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Integer getDevice() {
        return device;
    }

    public void setDevice(Integer device) {
        this.device = device;
    }


    @Override
    public String toString() {
        return "UserLog{" +
            "id=" + id +
            ", route=" + route +
            ", parameters=" + parameters +
            ", ip=" + ip +
            ", userid=" + userid +
            ", time=" + time +
            ", device=" + device +
        "}";
    }
}
