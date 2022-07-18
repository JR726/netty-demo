package com.wcx.demo.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.nio.charset.StandardCharsets;

/**
 * @author JR
 * @version 1.0
 * @since 2022.07.17
 */
public class NettyServer {
    private volatile EventLoopGroup bossGroup = new NioEventLoopGroup();
    private volatile EventLoopGroup workerGroup = new NioEventLoopGroup();

    public synchronized void start() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                 .channel(NioServerSocketChannel.class)
                 .option(ChannelOption.SO_BACKLOG, 1024)
                 .childOption(ChannelOption.SO_KEEPALIVE, true)
                 .childHandler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     protected void initChannel(SocketChannel socketChannel) {
                         socketChannel.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                             @Override
                             protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf o) {
                                 String result = o.toString(StandardCharsets.UTF_8);
                                 System.out.println(result);
                             }
                         });
                     }
                 });

        bootstrap.bind(30000);
    }

    public synchronized void stop() {
        if (this.bossGroup != null) {
            this.bossGroup.shutdownGracefully();
            this.bossGroup = null;
        }
        if (this.workerGroup != null) {
            this.workerGroup.shutdownGracefully();
            this.workerGroup = null;
        }
    }
}
