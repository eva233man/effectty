package com.hisporter.effectty.remoting;


/**
 * Created by zhangjp on 2016/3/10.
 */
public interface RemotingClient {
    void start();

    void connect(String ip, int listenPort) throws Exception;

    void shutdown();
}
