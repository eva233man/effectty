package com.sitech.billing.effectty.cluster;

import org.apache.commons.lang.StringUtils;

/**
 * @author zhangjp on 9/3/16.
 *         <p/>
 *         /QDETAIL/{集群名字}/DETAIL_SERVER/SERVER:\\192.168.0.150:8888?clusterName=HB_QDETAIL&identity=85750db6-e854-4eb3-a595-9227a5f2c8f6&createTime=1408189898185&isAvailable=true,SERVER
 *         <p/>
 */
public class NodeRegistryUtils {

    public static String getRootPath(String serverName, String clusterName) {
        return "/" + serverName + "/" + clusterName + "/SERVER";
    }

    /**
     * /QDETAIL/HB_QDETAIL/DETAIL_SERVER/SERVER:\\169.254.20.143:8099?
     * &clusterName=HB_QDETAIL&identity=60760CB89F5A4F16B9DCE8BCB65D0402&createTime=1457603168356&isAvailable=true&hostName=wangyla-PC
     */
    public static Node parse(String fullPath) {
        Node node = new Node();
        String[] nodeDir = fullPath.split("/");
        StringBuilder serverName = new StringBuilder();
        for (int i = 1; i < nodeDir.length - 3; i++) {
            serverName.append(nodeDir[i]).append("/");
        }

        node.setServerName(serverName.substring(0,serverName.length() - 1));
        node.setClusterName(nodeDir[nodeDir.length - 3]);
        String url = nodeDir[nodeDir.length-1];

        url = url.substring("SERVER".length() + 3);
        String address = url.split("\\?")[0];
        String ip = address.split(":")[0];

        node.setIp(ip);
        if (address.contains(":")) {
            String port = address.split(":")[1];
            if (port != null && !"".equals(port.trim())) {
                node.setPort(Integer.valueOf(port));
            }
        }
        String params = url.split("\\?")[1];

        String[] paramArr = params.split("&");
        for (String paramEntry : paramArr) {
            String key = paramEntry.split("=")[0];
            String value = paramEntry.split("=")[1];
            if ("clusterName".equals(key)) {
                node.setClusterName(value);
            } else if ("identity".equals(key)) {
                node.setIdentity(value);
            } else if ("createTime".equals(key)) {
                node.setCreateTime(Long.valueOf(value));
            } else if ("isAvailable".equals(key)) {
                node.setAvailable(Boolean.valueOf(value));
            } else if ("hostName".equals(key)) {
                node.setHostName(value);
            }
        }
        return node;
    }


    public static String getFullPath(Node node) {
        StringBuilder path = new StringBuilder();

        path.append(getRootPath(node.getServerName(), node.getClusterName()))
                .append("/SERVER")
                .append(":\\\\")
                .append(node.getIp());

        if (node.getPort() != null && node.getPort() != 0) {
            path.append(":").append(node.getPort());
        }

        path.append("?")
                .append("clusterName=")
                .append(node.getClusterName());

        path.append("&identity=")
                .append(node.getIdentity())
                .append("&createTime=")
                .append(node.getCreateTime())
                .append("&isAvailable=")
                .append(node.isAvailable())
                .append("&hostName=")
                .append(node.getHostName());

        return path.toString();
    }

    public static String getLoadBalancePath(Node node) {
        StringBuilder path = new StringBuilder();

        path.append(getRootPath(node.getServerName(), node.getClusterName()))
                .append("/SERVER/LB")
                .append(":\\\\")
                .append(node.getIp());

        if (node.getPort() != null && node.getPort() != 0) {
            path.append(":").append(node.getPort());
        }

        path.append("?")
                .append("clusterName=")
                .append(node.getClusterName());

        path.append("&identity=")
                .append(node.getIdentity())
                .append("&createTime=")
                .append(node.getCreateTime())
                .append("&isAvailable=")
                .append(node.isAvailable())
                .append("&hostName=")
                .append(node.getHostName());

        return path.toString();
    }

    public static String getRealRegistryAddress(String registryAddress) {
        if (StringUtils.isEmpty(registryAddress)) {
            throw new IllegalArgumentException("registryAddress is null！");
        }
        if (registryAddress.startsWith("zookeeper://")) {
            return registryAddress.replace("zookeeper://", "");
        }
        throw new IllegalArgumentException("illegal address protocol");
    }

    public static void main(String[] args) {
        String str110 = "/QDETAIL/HB_QDETAIL/DETAIL_SERVER/SERVER:\\\\172.21.4.110:8099?clusterName=HB_QDETAIL&identity=D68F5ADC34ED4B74AB59764B0F3D791B&createTime=1463574694666&isAvailable=true&hostName=B01";
        String str = "/BOSS_D/OFCS/DR0A/APP/QDETAIL/DETAIL_SERVER/SERVER:\\\\10.149.66.25:13001?clusterName=QDETAIL&identity=E1DC022CF26C4D86A5DB8D348BBA435F&createTime=1463573936001&isAvailable=true&hostName=ocshdp0";
        NodeRegistryUtils.parse(str);
    }
}
