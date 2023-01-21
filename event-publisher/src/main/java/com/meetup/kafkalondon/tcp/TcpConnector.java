package com.meetup.kafkalondon.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TcpConnector {
    Channel channel;
    String host;
    int port;
    EventLoopGroup workGroup = new NioEventLoopGroup();

    /**
     * Constructor
     *
     * @param port {@link Integer} port of server
     */
    public TcpConnector(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Shutdown connector
     */
    public void shutdown() {
        workGroup.shutdownGracefully();
    }

    /**
     * Startup connector
     *
     * @return {@link ChannelFuture}
     *
     * @throws Exception
     */
    public ChannelFuture startup() throws Exception {
        Bootstrap b = new Bootstrap();
        b.group(workGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.handler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new ChannelEventHandler());
            }
        });
        ChannelFuture channelFuture = b.connect(host, this.port).sync();
        this.channel = channelFuture.channel();

        return channelFuture;
    }
}
