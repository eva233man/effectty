package com.hisporter.effectty.log;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Access日志实现类：实际写的Fatal级别的日志
 * Created by zhangjp on 2016/4/7.
 */
public class AccessLoggerImpl implements AccessLogger{
    final transient Logger logger;
    static final String FQCN = AccessLoggerImpl.class.getName();

    public AccessLoggerImpl(Logger logger) {
        this.logger = logger;
    }
    public void access(String s) {
        this.logger.log(FQCN, Level.FATAL, s, (Throwable)null);
    }
}
