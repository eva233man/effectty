package com.sitech.billing.effectty.support.loadbalance;

import java.util.List;

public interface LoadBalance {

    public <S> S select(List<S> shards, String seed);

}
