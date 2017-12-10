package com.hisporter.effectty.zookeeper;

import com.hisporter.effectty.cluster.NodeRegistryUtils;
import com.hisporter.effectty.zookeeper.zkclient.ZkClientZkClient;

public class ZKFactory {
	public static ZKClient getZKClient(String registryAddress) {
		String address = registryAddress;
		if(registryAddress.startsWith("zookeeper://")){
			address = NodeRegistryUtils.getRealRegistryAddress(registryAddress);
		}
		return new ZkClientZkClient(address);
    }
}
