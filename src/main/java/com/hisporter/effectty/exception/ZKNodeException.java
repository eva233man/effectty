package com.hisporter.effectty.exception;

/**
 * zk 节点异常
 * Created by zhangjp on 2016/4/8.
 */
public class ZKNodeException extends Exception{

    private static final long serialVersionUID = -7804693020495753429L;

    public ZKNodeException() {
    }

    public ZKNodeException(String message) {
        super(message);
    }

    public ZKNodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZKNodeException(Throwable cause) {
        super(cause);
    }

}
