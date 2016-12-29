package com.sitech.billing.effectty.cluster;

import com.sitech.billing.effectty.registry.Registry;
import com.sitech.billing.effectty.registry.RegistryFactory;
import com.sitech.billing.effectty.remoting.RemotingServer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 抽象服务端的封装
 * Created by zhangjp on 2016/4/11.
 */
public abstract class AbstractServer {
    private AtomicBoolean started = new AtomicBoolean(false);
    //远程通信服务端
    protected RemotingServer remotingServer;
    //注册中心
    protected Registry registry;
    //节点信息
    protected Node node;
    //配置文件
    protected Config conf;

    /**
     * 对外暴露的启动接口
     */
    public void start(){
        if(started.compareAndSet(false, true)) {
            initconfig();
            remotingServer.start();
            register();

            afterStart();
        }

    }



    private void register() {
        //初始化注册中心
        registry = RegistryFactory.getRegistry(conf.getRegistryAddress());
        node = NodeFactory.create(Node.class, conf);
        registry.register(node);//注册节点信息到注册中心
    }

    private void initconfig() {
        beforeInitConfig();



        afterInitConfig();
    }

    /**
     * 对外暴露的优雅关闭的接口
     */
    public void stop(){
        beforeStop();
        if(started.compareAndSet(true, false)) {
            remotingServer.shutdown();
            if(registry != null){
                registry.unregister(node);
            }
        }
        afterStop();
    }

    abstract protected void beforeInitConfig();

    abstract protected void afterInitConfig();

    abstract protected void beforeStop();

    abstract protected void afterStop();

    abstract protected void afterStart();



    public int getListenPort() {
        return conf.getListenPort();
    }

    public void setListenPort(int listenPort) {
        this.conf.setListenPort(listenPort);
    }


    public void setConf(Config conf) {
        this.conf = conf;
    }

}
