package com.hisporter.effectty.registry;

import com.hisporter.effectty.registry.zookeeper.ZookeeperRegistry;

public class RegistryFactory {
	public static Registry getRegistry(String address) {

        if (address.startsWith("zookeeper://")) {
            return new ZookeeperRegistry(address);
        }
        throw new IllegalArgumentException("illegal address protocol");
    }
}
