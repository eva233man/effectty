package com.sitech.billing.effectty.common;

import java.util.regex.Pattern;

/**
 * Created by zhangjp on 2016/4/11.
 */
public class Constants {
    final static public String LOADBALANCE_PACKAGE = "com.sitech.billing.effectty.support.loadbalance."; /*负载包名*/

    final static public Pattern COMMA_SPLIT_PATTERN = Pattern.compile("\\s*[,]+\\s*");


    final static public String NETTY_WORKER_THREAD_NUM = "netty.worker.thread.num";
    final static public String NETTY_WORKER_SO_TIMEOUT = "netty.worker.so_timeout";
    final static public String NETTY_WORKER_SO_BACKLOG = "netty.worker.so_backlog";
    // 默认集群名字
    final static public String DEFAULT_CLUSTER_NAME = "defaultCluster";
}
