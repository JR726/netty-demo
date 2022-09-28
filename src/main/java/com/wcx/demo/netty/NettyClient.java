package com.wcx.demo.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;

/**
 * @author JR
 * @version 1.0
 * @since 2022.07.17
 */
public class NettyClient {
    private volatile EventLoopGroup worker;

    public ChannelHandlerContext channelHandlerContext;
    public Channel channel;

    /**
     * 生成客户端的密钥和证书：
     * keytool -genkey -alias clientalias -keysize 2048 -validity 365 -keyalg RSA -dname "CN=hw" -keypass clientpw -storepass clientpw -keystore client.jks
     * 将服务器端证书导入到客户端的证书仓库中：
     * keytool -import -trustcacerts -alias serveralias -file server.cer -storepass clientpw -keystore client.jks
     *
     * @return
     *
     * @throws Exception
     */
    public static SSLContext getClientContext() throws Exception {
        // 密钥库KeyStore
        KeyStore ks = KeyStore.getInstance("JKS");
        // 加载客户端证书
        InputStream in = new FileInputStream("C:\\Users\\39345\\client.jks");
        ks.load(in, "clientpw".toCharArray());
        // 信任库
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        // 初始化信任库
        tmf.init(ks);
        SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
        // 设置信任证书
        sslContext.init(null, tmf.getTrustManagers(), null);
        in.close();
        return sslContext;
    }

    public synchronized void start() throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        this.worker = new NioEventLoopGroup();
        bootstrap.group(worker)
                 .option(ChannelOption.SO_KEEPALIVE, true)
                 .option(ChannelOption.SO_REUSEADDR, true)
                 .channel(NioSocketChannel.class)
                 .handler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     protected void initChannel(SocketChannel ch) throws Exception {
                         SSLEngine sslEngine = getClientContext().createSSLEngine();
                         sslEngine.setUseClientMode(true); // 客户端模式
                         ch.pipeline().addLast(new SslHandler(sslEngine));
                         ch.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                             @Override
                             protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf o) {

                             }

                             @Override
                             public void channelActive(ChannelHandlerContext ctx) {
                                 channelHandlerContext = ctx;
                                 channel = ctx.channel();
                             }
                         });
                     }
                 });
        bootstrap.connect(new InetSocketAddress("127.0.0.1", 30000),
            new InetSocketAddress("127.0.0.1", 30001)).addListener(future -> {
            if (future.cause() != null) {
                System.out.println("建立连接失败: " + future.cause());
            }
        }).sync();
    }

    public synchronized void stop() {
        if (worker != null) {
            worker.shutdownGracefully();
            worker = null;
        }
    }

}
