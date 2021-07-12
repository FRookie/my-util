package org.example.common.pojo;

import io.vertx.core.json.JsonObject;

import java.util.HashMap;

/**
 *api接口返回结果封装
 *
 *@date 2020-12-17
 */
public class ResultBody {

    /**
     * code [0 成功, 1 失败, -1 没有权限(后台)]
     */
    private Integer code;
    private String message;
    private Object data;

    private ResultBody(Integer code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static String succeed(Object data){
        int code = 0;
        String message = "success";
        ResultBody resultBody = new ResultBody(code,message,data);
        return JsonObject.mapFrom(resultBody).toString();
    }

    public static String succeed(String message, Object data){
        int code = 0;
        message = message == null ? "success" : message;
        ResultBody resultBody = new ResultBody(code,message,data);
        return JsonObject.mapFrom(resultBody).toString();
    }

    public static String fail(Object data){
        int code = 1;
        String message = "failure";
        ResultBody resultBody = new ResultBody(code,message,data);
        return JsonObject.mapFrom(resultBody).toString();
    }

    public static String fail(String message, Object data){
        int code = 1;
        message = message == null ? "failure" : message;
        ResultBody resultBody = new ResultBody(code,message,data);
        return JsonObject.mapFrom(resultBody).toString();
    }

    public static String fail(Integer code,String message, Object data){
        ResultBody resultBody = new ResultBody(code == null ? 1 : code,message,data);
        return JsonObject.mapFrom(resultBody).toString();
    }

    public static String tableResult(int totalCount, Object item) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("count", totalCount);
        map.put("item", item);
        return succeed(map);
    }

    public static String noPermission(Object data) {
        int code = -1;
        String message = "required permission!";
        ResultBody resultBody = new ResultBody(code, message, data);
        return JsonObject.mapFrom(resultBody).toString();
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    /**
     * 判断ResultBody中的code是否为0
     * @return code为0返回true
     */
    public boolean succeeded() {
        return this.code == 0;
    }

    /**
     * 判断ResultBody中的code是否为 1 或 -1
     * @return code为 1 或 -1 返回true
     */
    public boolean failed() {
        return this.code == 1 || this.code == -1;
    }

    /**
     * 将resultBody toString之后的字符串转换回ResultBody对象
     * @param resultBodyStr resultBody toString之后的字符串
     * @return ResultBody对象
     */
    public static ResultBody convert2ResultBody(String resultBodyStr) {
        return convert2ResultBody(new JsonObject(resultBodyStr));
    }

    /**
     * 将resultBody toString之后的字符串转换回ResultBody对象
     * @param resultBodyJson resultBodyJson
     * @return ResultBody对象
     */
    public static ResultBody convert2ResultBody(JsonObject resultBodyJson) {
        return new ResultBody(resultBodyJson.getInteger("code"),
                resultBodyJson.getString("message"),
                resultBodyJson.getValue("data"));
    }
}
