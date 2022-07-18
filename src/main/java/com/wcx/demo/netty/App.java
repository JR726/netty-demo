package com.wcx.demo.netty;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author JR
 * @version 1.0
 * @since 2022-07-17
 */
public class App {
    public static void main(String[] args) throws InterruptedException {
        NettyServer nettyServer = new NettyServer();
        nettyServer.start();

        long start = LocalDateTime.now().plusSeconds(5).toInstant(ZoneOffset.of("+8")).toEpochMilli();
        long end = LocalDateTime.now().plusSeconds(4000).toInstant(ZoneOffset.of("+8")).toEpochMilli();

        NettyClient nettyClient = new NettyClient(true, start, end);
        nettyClient.start();

        ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
        ScheduledFuture<?> future = service.scheduleAtFixedRate(() -> System.out.println("ABC"), 0, 1, TimeUnit.NANOSECONDS);
        future.cancel(true);
        service.shutdownNow();

        // EventLoopGroup worker = new NioEventLoopGroup();
        // io.netty.util.concurrent.ScheduledFuture<?> scheduledFuture = worker.scheduleAtFixedRate(() -> System.out.println("DEF"), 0, 1,
        //     TimeUnit.SECONDS);
        // Thread.sleep(1000);
        // scheduledFuture.cancel(true);
    }
}
