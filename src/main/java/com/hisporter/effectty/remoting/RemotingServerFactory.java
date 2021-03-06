package com.hisporter.effectty.remoting;

import com.google.protobuf.MessageLite;
import com.hisporter.effectty.remoting.netty.NettyProtobufServer;
import com.hisporter.effectty.remoting.netty.NettyRemotingServer;
import com.hisporter.effectty.cluster.Config;

import java.util.Map;

/**
 * Created by zhangjp on 2016/4/19.
 */
public class RemotingServerFactory {

    public static RemotingServer getRemotingServer(String name, Map map) {

        if (name.equalsIgnoreCase("string")) {
            return new NettyRemotingServer((Config)map.get("CONF"),(String)map.get("HANDLER"));
        }
        else if (name.equalsIgnoreCase("protobuf")) {
            return new NettyProtobufServer((Config)map.get("CONF"),(String)map.get("HANDLER"),(MessageLite)map.get("LITE"));
        }
        return null;
    }
}
