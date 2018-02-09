package org.vog.common;

/**
 * 错误状态码定义
 */
public class ErrorCode {

    /**
     * 正常返回
     */
    public static int OK = 0;

    // 输入参数错误相关警告信息 *****************************************************
    /**
     * 缺少参数
     */
    public static int W1001 = 1001;
    /**
     * 由Hibernate Validator返回的异常信息
     */
    public static int W1002 = 1002;
    /**
     * (数组或输入值)长度超过限制
     */
    public static int W1003 = 1003;


    /**
     * 输入值必须是数字
     */
    public static int W1010 = 1010;
    /**
     * 输入文字长度必须是5个以上
     */
    public static int W1011 = 1011;
    /**
     * 输入文字长度不能超过500
     */
    public static int W1012 = 1012;

    // 业务异常 ********************************************************************
    /**
     * 表示业务级异常(未定义的业务异常)
     */
    public static int E5001 = 5001;

    /**
     * 用户名或密码不正确
     */
    public static int E5010 = 5010;
    /**
     * 用户不存在/已删除
     */
    public static int E5011 = 5011;

    /**
     * 表不存在
     */
    public static int E5101 = 5101;
    /**
     * 验证码无效/错误
     */
    public static int E5105 = 5105;




    // 系统级别异常 ********************************************************************
    /**
     * 表示系统级异常(业务未处理的异常)
     */
    public static int S9001 = 9001;

    /**
     * 用户未登录/会话过期
     */
    public static int S9004 = 9004;
    /**
     * http post请求时缺少csrf token
     */
    public static int S9005 = 9005;
    /**
     * http post请求时,csrf token错误
     */
    public static int S9006 = 9006;

    /**
     * http post请求时,重复提交
     */
    public static int S9008 = 9008;

}
