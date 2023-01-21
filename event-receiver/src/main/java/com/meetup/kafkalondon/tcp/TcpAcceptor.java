package com.meetup.kafkalondon.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

public class TcpAcceptor {

    private final EventLoopGroup bossLoopGroup;
    private final ChannelGroup channelGroup;
    private final PipelineFactory pipelineFactory;
    private final EventLoopGroup workerLoopGroup;

    public TcpAcceptor(Class<? extends PipelineFactory> pipelineFactoryType) {
        this.bossLoopGroup = new NioEventLoopGroup();
        this.workerLoopGroup = new NioEventLoopGroup();
        this.channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        try {
            this.pipelineFactory = pipelineFactoryType.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public final void shutdown() throws Exception {
        ((MessagePipelineFactory) pipelineFactory).getMessageHandler().emitMetrics();
        channelGroup.close();
        bossLoopGroup.shutdownGracefully();
        workerLoopGroup.shutdownGracefully();
    }

    public final void startup(int port) throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossLoopGroup, workerLoopGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.AUTO_CLOSE, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true);

        @SuppressWarnings("rawtypes")
        ChannelInitializer initializer = pipelineFactory.createInitializer();

        bootstrap.childHandler(initializer);

        try {
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            channelGroup.add(channelFuture.channel());
        } catch (Exception e) {
            shutdown();
            throw e;
        }
    }
}
