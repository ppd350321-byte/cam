package com.canteen.util;

public final class CacheKeyConstants {
    public static final String JWT_TOKEN = "jwt:token:%s";
    public static final String JWT_BLACKLIST = "jwt:blacklist:%s";
    public static final String JWT_USER_TOKEN = "jwt:user-token:%s:%d";
    public static final String MENU_CATEGORIES = "menu:categories";
    public static final String DISH_DETAIL = "dish:%d";
    public static final String USER_INFO = "user:info:%d";
    public static final String USER_PERMISSIONS = "user:permissions:%d";
    public static final String ORDER_DETAIL = "order:detail:%d";
    public static final String DASHBOARD_OVERVIEW = "dashboard:overview:%s";

    private CacheKeyConstants() {
    }
}
