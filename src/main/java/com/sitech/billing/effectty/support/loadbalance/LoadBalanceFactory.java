package com.sitech.billing.effectty.support.loadbalance;

import com.sitech.billing.effectty.common.Constants;

/**
 * Created by zhangjp on 2016/3/10.
 */
public class LoadBalanceFactory {

    /**
     * 根据 类名+LoadBalance（命名规范：后续如果增加别的算法，请按此规则） 来加载类实例
     * 如：一致性hash算法，传入 ConsistentHash
     * @param name
     * @return LoadBalance
     */
    public static LoadBalance create(String name){
        String className = Constants.LOADBALANCE_PACKAGE + name+"LoadBalance";
        LoadBalance loadbalance = null;
        try {
            loadbalance = LoadBalance.class.cast(Class.forName(className).newInstance());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return loadbalance;
    }
}
