package com.wcx.demo.netty;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author JR
 * @version 1.0
 * @since 2022-07-17
 */
public class App {
    public static void main(String[] args) {
        NettyServer nettyServer = new NettyServer();
        nettyServer.start();

        long start = LocalDateTime.now().plusSeconds(10).toInstant(ZoneOffset.of("+8")).toEpochMilli();
        long end = LocalDateTime.now().plusSeconds(40).toInstant(ZoneOffset.of("+8")).toEpochMilli();

        NettyClient nettyClient = new NettyClient(true, start, end);
        nettyClient.start();
    }
}
