package com.example.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Result<T> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private int code;
    private String message;
    private T data;

    public static final int SUCCESS = 200;
    public static final int ERROR = 500;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;

    private Result() {}

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = SUCCESS;
        result.message = "success";
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = SUCCESS;
        result.message = "success";
        result.data = data;
        return result;
    }

    public static <T> Result<T> success(int code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.code = ERROR;
        result.message = message;
        return result;
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }

    public static <T> Result<T> unauthorized(String message) {
        return error(UNAUTHORIZED, message);
    }

    public static <T> Result<T> forbidden(String message) {
        return error(FORBIDDEN, message);
    }

    // Getters
    public int getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }

    public String toJson() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "{\"code\":500,\"message\":\"JSON serialization error\"}";
        }
    }
}