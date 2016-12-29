package com.sitech.billing.effectty.remoting.netty;

import com.google.protobuf.MessageLite;
import com.sitech.billing.effectty.remoting.RemotingClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 封装netty客户端通信
 * Created by zhangjp on 2016/3/10.
 */
public class NettyProtobufClient implements RemotingClient {
    private static Logger LOGGER = LoggerFactory.getLogger(NettyProtobufClient.class);

    private final EventLoopGroup bossGroup;
    private final Bootstrap bootstrap;
    private final ChannelHandler clientHandler;
    private final MessageLite lite;

    public NettyProtobufClient(ChannelHandler clientHandler, MessageLite lite) {
        bossGroup = new NioEventLoopGroup(1);//客户端线程只需要起一个足够了，因为是短连接；一定要设置值，默认值是主机可用线程*2，会很占线程资源
        bootstrap = new Bootstrap();
        this.clientHandler = clientHandler;
        this.lite = lite;
    }

    public void start() {
        bootstrap.group(bossGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        ch.pipeline().addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
                        ch.pipeline().addLast("protobufDecoder", new ProtobufDecoder(lite));
                        //添加对象编码器 在服务器对外发送消息的时候自动将实现序列化的POJO对象编码
                        ch.pipeline().addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
                        ch.pipeline().addLast("protobufEncoder", new ProtobufEncoder());
                        pipeline.addLast(clientHandler);
                    }
                });


    }

    public void connect(String ip, int listenPort) throws Exception {
        ChannelFuture channelFuture = bootstrap.connect(ip, listenPort).sync();
        channelFuture.channel().closeFuture().sync();
    }

    public void shutdown() {
        bossGroup.shutdownGracefully();
    }
}
