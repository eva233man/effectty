package com.hisporter.effectty.cluster;

import com.hisporter.effectty.common.util.NetUtils;
import com.hisporter.effectty.support.SystemClock;

public class NodeFactory {

	public static <T extends Node> T create(Class<T> clazz, Config config) {
        try {
            T node = clazz.newInstance();
            node.setCreateTime(SystemClock.now());
            node.setIp(config.getIp());
            node.setHostName(NetUtils.getLocalHostName());
            node.setPort(config.getListenPort());
            node.setIdentity(config.getIdentity());
            node.setClusterName(config.getClusterName());
            node.setServerName(config.getServerName());
            return node;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
