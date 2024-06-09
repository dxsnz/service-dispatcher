package com.example.servicedispatcher.result;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回值
 *
 * @author xxChen
 * @since 2024-6-10 1:01
 */
public class Result {

    /*** "0-通过,1-提示,2-选择,3-禁止" */
    private String code;
    /*** 提示信息 */
    private String message;
    /*** 返回对象 */
    private HashMap<String, Object> data = new HashMap<>();

    /**
     * 构造器
     */
    private Result(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 通过
     */
    public static Result success() {
        return success(null);
    }
    public static Result success(String message) {
        return new Result("0", message);
    }

    /**
     * 提示
     */
    public static Result tip() {
        return tip(null);
    }
    public static Result tip(String message) {
        return new Result("1", message);
    }

    /**
     * 选择
     */
    public static Result choose() {
        return choose(null);
    }
    public static Result choose(String message) {
        return new Result("2", message);
    }

    /**
     * 禁止
     */
    public static Result fail() {
        return fail(null);
    }
    public static Result fail(String message) {
        return new Result("3", message);
    }

    /**
     * 插入数据
     */
    public Result putData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
    public Result putAllData(Map<String, Object> dataMap) {
        if (dataMap != null && !dataMap.isEmpty()) {
            this.data.putAll(dataMap);
        }
        return this;
    }

    public String getJsonString() {
        if (!this.data.containsKey("code")) {
            this.data.put("code", this.code);
        }
        if (!this.data.containsKey("message")) {
            this.data.put("message", this.message);
        }
        return JSONObject.toJSONString(this.data);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }
}
