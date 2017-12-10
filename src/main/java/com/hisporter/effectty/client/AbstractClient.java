package com.hisporter.effectty.client;

import com.hisporter.effectty.exception.ServerNotFoundException;
import com.hisporter.effectty.support.loadbalance.LoadBalance;
import com.hisporter.effectty.zookeeper.ZKFactory;
import com.hisporter.effectty.cluster.Node;
import com.hisporter.effectty.cluster.NodeRegistryUtils;
import com.hisporter.effectty.support.loadbalance.LoadBalanceFactory;
import com.sitech.billing.effectty.zookeeper.ZKClient;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 抽象客户端的封装
 * Created by zhangjp on 2016/4/11.
 */
public abstract class AbstractClient {
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractClient.class);

    // 服务端集群是否可用
    protected volatile boolean serverEnable = false;
    protected static List<Node> servers = new CopyOnWriteArrayList<Node>();
    protected ZKClient zkClient;
    protected LoadBalance loadBalance;
    protected String registryAddress;
    protected String loadBalanceClazz;

    // 服务端注册中心的根节点
    protected String rootPath;
    // 服务端调用统计
    protected ConcurrentHashMap<Node,Long> countNode = new ConcurrentHashMap<Node,Long>();

    protected AbstractClient(){
        init();
    }

    protected void init(){
        beforeInit();
        loadBalance = LoadBalanceFactory.create(loadBalanceClazz);
        zkClient = ZKFactory.getZKClient(registryAddress);
        loadServers();


        //增加zk连接状态的监听
        zkClient.addTargetStateListener(new IZkStateListener() {

            public void handleStateChanged(Watcher.Event.KeeperState keeperState) throws Exception {
                if (keeperState == Watcher.Event.KeeperState.Disconnected) {
                    serverEnable = false;
                } else if (keeperState == Watcher.Event.KeeperState.SyncConnected) {
                    serverEnable = true;
                } else if (keeperState == Watcher.Event.KeeperState.Expired) {
                    serverEnable = true;
                    recover();
                }
            }

            public void handleNewSession() throws Exception {

            }

            public void handleSessionEstablishmentError(Throwable throwable) throws Exception {

            }
        });
        /**
         * 增加服务端节点信息的监听
         * 如果存在变化，则更新变化到服务端列表servers
         */
        zkClient.addTargetChildListener(rootPath, new ZkChildChangeListener());


        afterInit();
    }


    /**
     * 重连
     */
    private void recover() {
        init();//重新初始化
    }



    abstract protected void beforeInit();

    abstract protected void afterInit();


    protected void loadServers() {
        Boolean serverExists = zkClient.exists(rootPath);
        if (serverExists) {
            List<String> nodes = zkClient.getChildren(rootPath);
            for (String node : nodes) {
                Node server = NodeRegistryUtils.parse(rootPath + "/" + node);
                servers.add(server);
                LOGGER.debug("Node:"+server.toFullString());
                countNode.putIfAbsent(server, 0L);
            }
            if(nodes.size()>0){
                serverEnable = true;
            }
        }
        else {
            throw new RuntimeException("servers not exist! 服务端没启动！");
        }
    }

    protected Node getServerNode(String hashCode) {
        try {
            if (servers.size() == 0) {
                throw new ServerNotFoundException("no available server!");
            }
            // 连服务端的负载均衡算法
            return loadBalance.select(servers, hashCode);
        } catch (ServerNotFoundException e) {
            this.serverEnable = false;
            // publish msg
            // EventInfo eventInfo = new
            // EventInfo(EcTopic.NO_JOB_TRACKER_AVAILABLE);
            // appContext.getEventCenter().publishAsync(eventInfo);
            e.printStackTrace();
        }
        return null;
    }


    public ConcurrentHashMap<Node, Long> getCountNode() {
        return countNode;
    }



    public class ZkChildChangeListener implements IZkChildListener {

        public void handleChildChange(String s, List<String> currentChilds) throws Exception {
            LOGGER.debug("addTargetChildListener : " + currentChilds.toString());

            List<Node> newServers = new CopyOnWriteArrayList<Node>();
            //新增的节点加入
            for (String node : currentChilds) {
                Node server = NodeRegistryUtils.parse(rootPath + "/" + node);
                if (!servers.contains(server)) {
                    servers.add(server);
                    countNode.put(server, 0L);
                }
                newServers.add(server);
            }
            //宕掉的节点去掉
            for (Node node : servers) {
                if (!newServers.contains(node)) {
                    servers.remove(node);
                }
            }
            //需要补一条监听，一个监听只能监听一次
            zkClient.addTargetChildListener(rootPath, new ZkChildChangeListener());
        }
    }
}
