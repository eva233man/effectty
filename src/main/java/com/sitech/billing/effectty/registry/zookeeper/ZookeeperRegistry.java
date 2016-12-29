package com.sitech.billing.effectty.registry.zookeeper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sitech.billing.effectty.cluster.Node;
import com.sitech.billing.effectty.registry.Registry;
import com.sitech.billing.effectty.zookeeper.ZKClient;
import com.sitech.billing.effectty.zookeeper.ZKFactory;

public class ZookeeperRegistry implements Registry{

	protected final static Logger LOGGER = LoggerFactory.getLogger(Registry.class);
	private ZKClient zkClient;
	
	
	public ZookeeperRegistry(String address){
		zkClient = ZKFactory.getZKClient(address);
		
	}


	@Override
	public void register(Node node) {
		if (zkClient.exists(node.toFullString())) {
            return;
        }
		LOGGER.debug("server register : "+ node.toFullString());
        zkClient.create(node.toFullString(), true, false);
	}


	@Override
	public void unregister(Node node) {
		zkClient.delete(node.toFullString());
	}


	@Override
	public void destroy() {
		try {
            zkClient.close();
        } catch (Exception e) {
            LOGGER.warn("Failed to close zookeeper client , cause: " + e.getMessage(), e);
        }
		
	}
}
