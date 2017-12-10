package com.hisporter.effectty.log;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Priority;

/**
 * 重写log4jFileAppender，ERROR级别以上的日志分开输出
 * Created by wangyla on 2016/3/30.
 */
public class LoggerAppender extends DailyRollingFileAppender{

    @Override
    public boolean isAsSevereAsThreshold(Priority priority) {
        if(priority.isGreaterOrEqual(Priority.FATAL)){
            return this.threshold.equals(priority);
        } else {
            return threshold == null || priority.isGreaterOrEqual(threshold);
        }
    }
}
