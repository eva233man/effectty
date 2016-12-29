package com.sitech.billing.effectty.remoting.netty;

import com.sitech.billing.effectty.remoting.RemotingClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 封装netty客户端通信
 * Created by zhangjp on 2016/3/10.
 */
public class NettyRemotingClient implements RemotingClient {
    private static Logger LOGGER = LoggerFactory.getLogger(NettyRemotingClient.class);

    private final EventLoopGroup bossGroup;
    private final Bootstrap bootstrap;
    private ChannelHandler clientHandler;
    private int baseFrameSize = 1024;

    public NettyRemotingClient(ChannelHandler clientHandler) {
        bossGroup = new NioEventLoopGroup(1);//客户端线程只需要起一个足够了，因为是短连接；一定要设置值，默认值是主机可用线程*2，会很占线程资源
        bootstrap = new Bootstrap();
        this.clientHandler = clientHandler;
    }

    public void start() {
        bootstrap.group(bossGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new LineBasedFrameDecoder(baseFrameSize));
                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(clientHandler);
                    }
                });


    }

    public void connect(String ip, int listenPort) throws Exception {
        ChannelFuture channelFuture = bootstrap.connect(ip, listenPort).sync();
        channelFuture.channel().closeFuture().sync();
    }

    public int getBaseFrameSize() {
        return baseFrameSize;
    }

    public void setBaseFrameSize(int baseFrameSize) {
        this.baseFrameSize = baseFrameSize;
    }

    public ChannelHandler getClientHandler() {
        return clientHandler;
    }

    public void setClientHandler(ChannelHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    public void shutdown() {
        bossGroup.shutdownGracefully();
    }
}
