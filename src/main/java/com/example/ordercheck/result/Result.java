package com.example.ordercheck.result;

import java.util.HashMap;

/**
 * 返回值
 *
 * @author xxChen
 * @since 2024-6-5 10:39
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
    public Result(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public boolean isSuccess() {
        return this.code.equals("1");
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
