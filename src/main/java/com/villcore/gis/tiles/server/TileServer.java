package com.villcore.gis.tiles.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TileServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TileServer.class);

    public static void start(String district, int listenPort, TilePackageManager tilePackageManager) {
        EventLoopGroup bossLoopGroup = new NioEventLoopGroup(1);
        EventLoopGroup workLoopGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossLoopGroup, workLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1 * 60 * 60 * 1000)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_RCVBUF, 128 * 1024)
                    .childOption(ChannelOption.SO_SNDBUF, 128 * 1024)
                    .childHandler(new ChildChannelHandlerInitlizer(district, tilePackageManager));
            serverBootstrap.bind(Integer.valueOf(listenPort)).sync().channel().closeFuture().sync();
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
        } finally {
            bossLoopGroup.shutdownGracefully();
            workLoopGroup.shutdownGracefully();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.debug("need close tile server ...");
                bossLoopGroup.shutdownGracefully();
                workLoopGroup.shutdownGracefully();
                LOGGER.debug("tile server closed ...");
            }
        }));
    }

    public static void main(String[] args) {
        Path tileRoot = Paths.get("E:\\map_tiles\\");
        Path emptyImage = Paths.get("E:\\map_tiles\\empty.png");

        try {
            TilePackageManager tilePackageManager = new TilePackageManager(tileRoot, emptyImage);
            tilePackageManager.init();

            //TODO 配置信息需要从文件中读取
            String listenPort = "8082";
            String district = "beijing";

            LOGGER.debug("tile server starting ..., listen port [{}] ...", listenPort);
            start(district, Integer.valueOf(listenPort), tilePackageManager);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

    }
}
