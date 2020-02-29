package com.leyou.common.auth.entity;

public class UserHolder {

    private static ThreadLocal<String> threadLocal = new ThreadLocal<String>();

    private static String userId;

    public static String getUserId() {
        return threadLocal.get();
    }

    public static void setUserId(String userId) {
        threadLocal.set(userId);
    }

    public static void removeUserId() {
        threadLocal.remove();
    }
}
