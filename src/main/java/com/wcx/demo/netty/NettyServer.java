package com.wcx.demo.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

/**
 * @author JR
 * @version 1.0
 * @since 2022.07.17
 */
public class NettyServer {
    private volatile EventLoopGroup bossGroup = new NioEventLoopGroup();
    private volatile EventLoopGroup workerGroup = new NioEventLoopGroup();

    /**
     * 生成服务端的密钥和证书：
     * keytool -genkey -alias serveralias -keysize 2048 -validity 365 -keyalg RSA -dname "CN=hw" -keypass serverpw -storepass serverpw -keystore server.jks
     * 生成服务器端自签名证书：
     * keytool -export -alias serveralias -keystore server.jks -storepass serverpw -file server.cer
     *
     * @return
     *
     * @throws Exception
     */
    public static SSLContext getServerContext() throws Exception {
        // 密钥库KeyStore
        KeyStore ks = KeyStore.getInstance("JKS");
        // 加载服务端证书
        InputStream in = new FileInputStream("C:\\Users\\39345\\server.jks");
        // 加载服务端的KeyStore，该密钥库的密码，storepass指定密钥库的密码(获取keystore信息所需的密码)
        ks.load(in, "serverpw".toCharArray());
        // 密钥管理器
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        // 初始化密钥管理器, keypass 指定别名条目的密码(私钥的密码)
        kmf.init(ks, "serverpw".toCharArray());
        // 获取安全套接字协议（TLS协议）的对象
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        // 参数一：要给对端认证的密钥
        // 参数二：对等信任认证，如果双向认证就写成tf.getTrustManagers()
        // 参数三：伪随机数生成器
        sslContext.init(kmf.getKeyManagers(), null, null);
        in.close();
        return sslContext;
    }

    public synchronized void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                 .channel(NioServerSocketChannel.class)
                 .option(ChannelOption.SO_BACKLOG, 1024)
                 .childOption(ChannelOption.SO_KEEPALIVE, true)
                 .childHandler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     protected void initChannel(SocketChannel ch) throws Exception {
                         SSLEngine sslEngine = getServerContext().createSSLEngine();
                         sslEngine.setUseClientMode(false); // 服务端模式
                         ch.pipeline().addLast(new SslHandler(sslEngine));
                         ch.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                             @Override
                             protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf o) {
                                 System.out.println("接收到：" + o.toString(StandardCharsets.UTF_8));
                             }
                         });
                     }
                 });

        bootstrap.bind("127.0.0.1", 30000).addListener(future -> {
            if (future.cause() != null) {
                System.out.println("绑定失败: " + future.cause());
            }
        }).sync();
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
