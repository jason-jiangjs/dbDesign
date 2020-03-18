package org.dbm.common;

/**
 * 常量定义
 */
public class Constants {

    // 系统缺省编码
    public static final String DefaultEncode = "UTF-8";

    // 存放在http attribute中表示用户ID的变量名
    public static final String KEY_USER_ID = "_CUSTOMER_ID";
    public static final String KEY_USER_NAME = "_CUSTOMER_NAME";

    public static final String KEY_DB_ID = "_dbId";

    /**
     * 第三方登录定义
     */
    public enum ThirdLogin {
        GITLAB("GitLab");

        private ThirdLogin(String idVal) {
            this.userId = idVal;
        }
        public String getValue() {
            return userId;
        }
        private String userId;
    }
}
