package com.Ink.custom;

/**
 * @ClassName LoginUserInfoHelper
 * @Description TODO 线程存储用户信息
 * @Author Ink
 */
public class LoginUserInfoHelper {
    private static final ThreadLocal<Long> userId = new ThreadLocal<Long>();
    private static final ThreadLocal<String> username = new ThreadLocal<String>();

    public static void setUserId(Long _userId) {
        userId.set(_userId);
    }
    public static Long getUserId() {
        return userId.get();
    }
    public static void removeUserId() {
        userId.remove();
    }
    public static void setUsername(String _username) {
        username.set(_username);
    }
    public static String getUsername() {
        return username.get();
    }
    public static void removeUsername() {
        username.remove();
    }
}