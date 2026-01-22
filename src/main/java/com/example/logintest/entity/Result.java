package com.example.logintest.entity;

/**
 * 统一返回结果
 */
public class Result<T> {

    // 状态码常量
    public static final int SUCCESS = 200;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int SESSION_CONFLICT = 409;
    public static final int SERVER_ERROR = 500;

    private Integer code;
    private String message;
    private T data;

    public Result() {
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 成功
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(SUCCESS, message, data);
    }

    public static <T> Result<T> success(String message) {
        return new Result<>(SUCCESS, message, null);
    }

    // 失败
    public static <T> Result<T> error(String message) {
        return new Result<>(BAD_REQUEST, message, null);
    }

    // 重载error方法，支持自定义状态码
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    // Getter 和 Setter
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}