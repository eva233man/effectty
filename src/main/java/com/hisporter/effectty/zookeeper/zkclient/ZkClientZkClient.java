package com.hisporter.effectty.zookeeper.zkclient;

import com.hisporter.effectty.exception.ZKNodeException;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import com.sitech.billing.effectty.zookeeper.ZKClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ZkClientZkClient implements ZKClient {

    private static final Logger logger = LoggerFactory.getLogger(ZkClientZkClient.class);
    public static final int connectionTimeout = 30000;

    private final ZkClient zkClient;

    private volatile KeeperState state = KeeperState.SyncConnected;

    public ZkClientZkClient(String address) {
        zkClient = new ZkClient(address, connectionTimeout);
    }

    public String create(String path, boolean ephemeral, boolean sequential) {
        if (ephemeral) {
            return createEphemeral(path, sequential);
        } else {
            return createPersistent(path, sequential);
        }
    }

    public String create(String path, Object data, boolean ephemeral, boolean sequential) {
        if (ephemeral) {
            return createEphemeral(path, data, sequential);
        } else {
            logger.info("path="+path+";data="+data.toString());
            return createPersistent(path, data, sequential);
        }
    }

    protected String createPersistent(String path, boolean sequential) {
        try {
            if (sequential) {
                return zkClient.createPersistentSequential(path, true);
            } else {
                zkClient.createPersistent(path, true);
                return path;
            }
        } catch (ZkNodeExistsException ignored) {
        }
        return null;
    }

    protected String createPersistent(String path, Object data, boolean sequential) {
        try {
            if (sequential) {
                zkClient.createPersistentSequential(path, true);
                setData(path,data);
                return path;
            } else {
                zkClient.createPersistent(path, true);
                setData(path,data);
                return path;
            }
        } catch (ZkNodeExistsException ignored) {
        }
        return null;
    }

    protected String createEphemeral(String path, boolean sequential) {
        try {
            if (sequential) {
                return zkClient.createEphemeralSequential(path, true);
            } else {
                zkClient.createEphemeral(path);
                return path;
            }
        } catch (ZkNodeExistsException ignored) {
        }
        return null;
    }

    protected String createEphemeral(String path, Object data, boolean sequential) {
        try {
            if (sequential) {
                zkClient.createEphemeralSequential(path, true);
                setData(path,data);
                return path;
            } else {
                zkClient.createEphemeral(path, data);
                return path;
            }
        } catch (ZkNodeExistsException ignored) {
        }
        return null;
    }

    public void addTargetStateListener(IZkStateListener listener){
        zkClient.subscribeStateChanges(listener);
    }

    public List<String> addTargetChildListener(String path, IZkChildListener iZkChildListener) {
        return zkClient.subscribeChildChanges(path, iZkChildListener);
    }

    public void removeTargetChildListener(String path, IZkChildListener iZkChildListener) {
        zkClient.unsubscribeChildChanges(path, iZkChildListener);
    }

    public void addTargetDataListener(String path, IZkDataListener iZkDataListener) {
        zkClient.subscribeDataChanges(path,iZkDataListener);
    }

    public void removeTargetDataListener(String path, IZkDataListener iZkDataListener) {
        zkClient.unsubscribeDataChanges(path, iZkDataListener);
    }


    public boolean delete(String path) {
        try {
            return zkClient.delete(path);
        } catch (ZkNoNodeException ignored) {
        }
        return false;
    }

    public boolean exists(String path) {
        try {
            return zkClient.exists(path);
        } catch (ZkNoNodeException ignored) {
        }
        return false;
    }

    public <T> T getData(String path) throws ZKNodeException {
        try {
            return zkClient.readData(path);

        } catch (ZkNoNodeException ex) {
            throw new ZKNodeException(ex);
        }
    }

    public void setData(String path, Object data) {
        zkClient.writeData(path, data);
    }

    public List<String> getChildren(String path) {
        try {
            return zkClient.getChildren(path);
        } catch (ZkNoNodeException e) {
            return null;
        }
    }

    public boolean isConnected() {
        return state == KeeperState.SyncConnected;
    }

    public void close() {
        zkClient.close();
    }
}
