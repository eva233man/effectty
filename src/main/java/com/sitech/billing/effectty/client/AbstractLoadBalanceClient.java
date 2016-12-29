package com.sitech.billing.effectty.client;

import com.sitech.billing.effectty.cluster.Config;
import com.sitech.billing.effectty.cluster.Node;
import com.sitech.billing.effectty.cluster.NodeFactory;
import com.sitech.billing.effectty.cluster.NodeRegistryUtils;
import com.sitech.billing.effectty.exception.ServerNotFoundException;
import com.sitech.billing.effectty.support.loadbalance.LoadBalance;
import com.sitech.billing.effectty.support.loadbalance.LoadBalanceFactory;
import com.sitech.billing.effectty.zookeeper.ZKClient;
import com.sitech.billing.effectty.zookeeper.ZKFactory;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 抽象长连接客户端的封装
 * 保持连接，客户端单节点，做会话保持和任务分配
 * Created by zhangjp on 2016/4/11.
 */
public abstract class AbstractLoadBalanceClient {
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractLoadBalanceClient.class);

    // 服务端集群是否可用
    protected volatile boolean serverEnable = false;
    protected static List<Node> servers = new CopyOnWriteArrayList<Node>();
    protected ZKClient zkClient;
    protected LoadBalance loadBalance;
    protected String registryAddress;
    protected String loadBalanceClazz;
    //节点信息
    protected Node node;

    // 服务端注册中心的根节点
    protected String rootPath;
    protected String clientPath;
    //配置文件
    protected Config conf;
    // 服务端调用统计
    protected ConcurrentHashMap<Node,Long> countNode = new ConcurrentHashMap<Node,Long>();

    protected AbstractLoadBalanceClient(){
        init();
    }

    protected void init(){
        beforeInit();
        loadBalance = LoadBalanceFactory.create(loadBalanceClazz);
        zkClient = ZKFactory.getZKClient(registryAddress);
        /**
         * 注册客户端节点，抢占式
         * 如果节点存在并且不为空，则表示已经有节点已抢占上
         */
        clientPath = rootPath + "/LB";
        node = NodeFactory.create(Node.class, conf);
        if (zkClient.exists(clientPath) && zkClient.getChildren(clientPath)!=null) {
            serverEnable = false;
            zkClient.addTargetChildListener(clientPath, new ZkLBChildChangeListener());
        }else {
            //创建临时节点，一旦程序宕掉，则节点消失
            zkClient.create(NodeRegistryUtils.getLoadBalancePath(node), true, false);
            serverEnable = true;
        }

        //增加zk连接状态的监听
        zkClient.addTargetStateListener(new IZkStateListener() {

            public void handleStateChanged(Watcher.Event.KeeperState keeperState) throws Exception {
                if (keeperState == Watcher.Event.KeeperState.Disconnected) {
                    serverEnable = false;
                } else if (keeperState == Watcher.Event.KeeperState.SyncConnected) {

                } else if (keeperState == Watcher.Event.KeeperState.Expired) {
                    recover();
                }
            }

            public void handleNewSession() throws Exception {

            }

            public void handleSessionEstablishmentError(Throwable throwable) throws Exception {

            }
        });




        if(serverEnable){
            loadServers();
            /**
             * 增加服务端节点信息的监听
             * 如果存在变化，则更新变化到服务端列表servers
             */
            zkClient.addTargetChildListener(rootPath, new ZkChildChangeListener());

            afterInit();
        }


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
            e.printStackTrace();
        }
        return null;
    }


    public ConcurrentHashMap<Node, Long> getCountNode() {
        return countNode;
    }

    public class ZkLBChildChangeListener implements IZkChildListener {

        public void handleChildChange(String s, List<String> currentChilds) throws Exception {
            LOGGER.debug("addTargetChildListener : " + currentChilds.toString());

            /**
             * 如果节点有变化，则重新初始化
             */
            recover();

        }
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
