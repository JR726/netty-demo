package com.wcx.demo.netty;

import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

/**
 * @author JR
 * @version 1.0
 * @since 2022-09-28
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        NettyServer server = new NettyServer();
        server.start();

        Thread.sleep(1000);

        NettyClient client = new NettyClient();
        client.start();

        Thread.sleep(1000);

        client.channel.writeAndFlush(Unpooled.copiedBuffer("AAA".getBytes(StandardCharsets.UTF_8)));
    }
}
