# effectty
分布式远程通讯框架
分为服务端和客户端两部分

1.服务端使用zookeeper做为注册中心
2.使用netty做为主通讯服务socket服务，也可以使用别的框架，有预留接口


客户端根据zookeeper注册中心的节点来选取要访问的节点，使用hash一致性算法
