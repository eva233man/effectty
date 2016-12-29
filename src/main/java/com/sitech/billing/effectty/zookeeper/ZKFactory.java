package com.sitech.billing.effectty.zookeeper;

import com.sitech.billing.effectty.cluster.NodeRegistryUtils;
import com.sitech.billing.effectty.zookeeper.zkclient.ZkClientZkClient;

public class ZKFactory {
	public static ZKClient getZKClient(String registryAddress) {
		String address = registryAddress;
		if(registryAddress.startsWith("zookeeper://")){
			address = NodeRegistryUtils.getRealRegistryAddress(registryAddress);
		}
		return new ZkClientZkClient(address);
    }
}
