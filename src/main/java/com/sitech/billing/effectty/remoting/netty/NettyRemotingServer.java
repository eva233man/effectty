package com.sitech.billing.effectty.remoting.netty;

import com.sitech.billing.effectty.common.Constants;
import com.sitech.billing.effectty.remoting.RemotingServer;
import com.sitech.billing.effectty.cluster.Config;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * Created by zhangjp on 2016/3/10.
 */
public class NettyRemotingServer implements RemotingServer {


    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workGroup;
    private final ServerBootstrap serverBootstrap;
    private Config conf;
    private String handler;

    public NettyRemotingServer(Config conf, String handler) {
        this.conf = conf;
        bossGroup = new NioEventLoopGroup(1);
        workGroup = new NioEventLoopGroup(conf.getParameter(Constants.NETTY_WORKER_THREAD_NUM, 10));
        serverBootstrap = new ServerBootstrap();
        this.handler = handler;
    }

    public void start() {
        serverBootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, Integer.parseInt(conf.getParameter(Constants.NETTY_WORKER_SO_BACKLOG)))//设置客户端连接的排队数，可以设置小点，或者默认系统的
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_TIMEOUT, Integer.parseInt(conf.getParameter(Constants.NETTY_WORKER_SO_TIMEOUT))) //设置读数据的超时时间，以毫秒为单位
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new LineBasedFrameDecoder(conf.getParameter("baseFrameSize", 2048)));
                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(ChannelHandler.class.cast(Class.forName(handler).newInstance()));
                    }
                });

        try {
            this.serverBootstrap.bind(conf.getListenPort()).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void shutdown() {
        workGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }
}
