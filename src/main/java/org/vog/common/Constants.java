package org.vog.common;

/**
 * Created by yangjindong on 2016/12/21.
 */
public class Constants {

    // 系统缺省编码
    public static final String DefaultEncode = "UTF-8";

    // 存放在session中的验证码的key(登录时)
    public static final String LOGIN_VERIFYCODE_KEY = "__login_verifycode_key_";
    // 存放在session中的验证码的有效时间(登录时)
    public static final String LOGIN_VERIFYCODE_UPDATETIME = "__login_verifycode_updatetime_";
    // 存放在session中的密码输错次数的key(登录时)
    public static final String PASSWORD_ERROR_COUNT_KEY = "__password_error_count_key_";

    // 存放在http attribute中表示用户ID的变量名
    public static final String KEY_USER_ID = "_CUSTOMER_ID";
    public static final String KEY_CURR_DB_ID = "_CURR_DB_ID";
    public static final String KEY_USER_NAME = "_CUSTOMER_NAME";



}
