package com.sitech.billing.effectty.support.loadbalance;


import com.sitech.billing.effectty.support.ConsistentHashSelector;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 一致性hash算法
 */
public class ConsistentHashLoadBalance extends AbstractLoadBalance {

    @Override
    protected <S> S doSelect(List<S> shards, String seed) {
        if(seed == null || seed.length() == 0){
            seed = "HASH-".concat(String.valueOf(ThreadLocalRandom.current().nextInt()));
        }
        ConsistentHashSelector<S> selector = new ConsistentHashSelector<S>(shards);
        return selector.selectForKey(seed);
    }
}
