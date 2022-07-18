package com.wcx.demo.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.ScheduledFuture;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author JR
 * @version 1.0
 * @since 2022.07.17
 */
public class NettyClient {
    private final boolean period;

    private final long start;

    private final long end;

    private volatile EventLoopGroup worker;

    public NettyClient(boolean period, long start, long end) {
        this.period = period;
        this.start = start;
        this.end = end;
    }

    public synchronized void start() {
        Bootstrap bootstrap = new Bootstrap();
        this.worker = new NioEventLoopGroup();
        bootstrap.group(worker).option(ChannelOption.SO_KEEPALIVE, true)
                 .channel(NioSocketChannel.class)
                 .handler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     protected void initChannel(SocketChannel socketChannel) {
                         socketChannel.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                             @Override
                             protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf o) {

                             }

                             @Override
                             public void channelActive(ChannelHandlerContext ctx) {
                                 long initialDelay = start - System.currentTimeMillis();
                                 if (initialDelay < 0) {
                                     initialDelay = 0;
                                 }
                                 if (period) {
                                     ScheduledFuture<?> future = worker.scheduleAtFixedRate(
                                         () -> {
                                             ctx.channel()
                                                .writeAndFlush(Unpooled.copiedBuffer("Hello", StandardCharsets.UTF_8));
                                             worker.schedule(() -> {System.out.println(Thread.currentThread().getName());}, 0,
                                                 TimeUnit.MILLISECONDS);
                                             worker.schedule(() -> {System.out.println(Thread.currentThread().getName());}, 10,
                                                 TimeUnit.MILLISECONDS);
                                             worker.schedule(() -> {System.out.println(Thread.currentThread().getName());}, 20,
                                                 TimeUnit.MILLISECONDS);
                                         },
                                         initialDelay,
                                         1000, TimeUnit.MILLISECONDS);
                                     worker.schedule(() -> {
                                         System.out.println("取消了");
                                         future.cancel(true);
                                     }, 20, TimeUnit.SECONDS);
                                 } else {
                                     worker.scheduleWithFixedDelay(
                                         () -> ctx.channel()
                                                  .writeAndFlush(Unpooled.copiedBuffer("Hello", StandardCharsets.UTF_8)),
                                         initialDelay,
                                         1, TimeUnit.MILLISECONDS);
                                 }
                             }
                         });
                     }
                 });
        for (int i = 1; i <= 1; i++) {
            bootstrap.connect(new InetSocketAddress("127.0.0.1", 30000), new InetSocketAddress("127.0.0.1", 30000 + i));
        }

        long delay = end - System.currentTimeMillis();
        if (delay <= 0) {
            this.worker.shutdownGracefully();
        } else {
            this.worker.schedule(this::stop, delay, TimeUnit.MILLISECONDS);
        }
    }

    public synchronized void stop() {
        if (worker != null) {
            worker.shutdownGracefully();
            worker = null;
        }
    }
}
