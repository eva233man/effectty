package com.sitech.billing.effectty.registry;

import com.sitech.billing.effectty.registry.zookeeper.ZookeeperRegistry;

public class RegistryFactory {
	public static Registry getRegistry(String address) {

        if (address.startsWith("zookeeper://")) {
            return new ZookeeperRegistry(address);
        }
        throw new IllegalArgumentException("illegal address protocol");
    }
}
