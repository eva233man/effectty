package com.hisporter.effectty.registry;

import com.hisporter.effectty.cluster.Node;

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
