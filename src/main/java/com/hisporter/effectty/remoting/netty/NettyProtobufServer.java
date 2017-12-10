package com.hisporter.effectty.remoting.netty;

import com.google.protobuf.MessageLite;
import com.hisporter.effectty.common.Constants;
import com.hisporter.effectty.remoting.RemotingServer;
import com.hisporter.effectty.cluster.Config;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

/**
 * Created by zhangjp on 2016/3/10.
 */
public class NettyProtobufServer implements RemotingServer {


    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workGroup;
    private final ServerBootstrap serverBootstrap;
    private final Config conf;
    private final String handler;
    private final MessageLite lite;

    public NettyProtobufServer(Config conf, String handler, MessageLite lite) {
        this.conf = conf;
        bossGroup = new NioEventLoopGroup(1);
        workGroup = new NioEventLoopGroup(conf.getParameter(Constants.NETTY_WORKER_THREAD_NUM, 10));
        serverBootstrap = new ServerBootstrap();
        this.handler = handler;
        this.lite = lite;
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
                        ch.pipeline().addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
                        ch.pipeline().addLast("protobufDecoder", new ProtobufDecoder(lite));
                        ch.pipeline().addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
                        ch.pipeline().addLast("protobufEncoder", new ProtobufEncoder());
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
