package com.sitech.billing.effectty.exception;

/**
 * Created by zhangjp on 2016/3/10.
 * 当没有 找到 Server 节点的时候抛出这个异常
 */
public class ServerNotFoundException extends Exception{

    private static final long serialVersionUID = -7804693020495753429L;

    public ServerNotFoundException() {
    }

    public ServerNotFoundException(String message) {
        super(message);
    }

    public ServerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerNotFoundException(Throwable cause) {
        super(cause);
    }

}
