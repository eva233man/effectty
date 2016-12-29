package com.sitech.billing.effectty.registry;

import com.sitech.billing.effectty.cluster.Node;

public interface Registry {
    /**
     * 节点注册
     *
     * @param node
     */
    void register(Node node);

    /**
     * 节点 取消注册
     *
     * @param node
     */
    void unregister(Node node);


    /**
     * 销毁
     */
    void destroy();

}
