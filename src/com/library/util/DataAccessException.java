package com.library.util;

/**
 * 数据访问异常，统一包装 SQLException，便于页面显示友好提示。
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
