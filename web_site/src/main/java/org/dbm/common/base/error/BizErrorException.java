package org.dbm.common.base.error;


/**
 * @Description 业务逻辑 运行时异常
 *
 */
public class BizErrorException extends RuntimeException
{

    private int errCode;
    private Object[] argArray;


    public BizErrorException(String message)
    {
        super(message);
    }

    public BizErrorException(String message, Throwable e) {
        super(message, e);
    }

    public BizErrorException(Throwable e) {
        super(e);
    }
    /**
     * 返回异常响应结果，包括error code和简短异常信息说明
     */
    public BizErrorException(int code, String message, Object... args)
    {
        super(message);
        this.errCode = code;
        argArray = args;
    }

    public int getErrCode()
    {
        return errCode;
    }

    public void setErrCode(int errCode)
    {
        this.errCode = errCode;
    }

    public Object[] getArgArray()
    {
        return argArray;
    }

    public void setArgArray(Object[] argArray)
    {
        this.argArray = argArray;
    }
}
