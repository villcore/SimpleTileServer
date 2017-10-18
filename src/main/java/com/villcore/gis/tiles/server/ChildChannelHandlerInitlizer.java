package com.villcore.gis.tiles.server;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

public class ChildChannelHandlerInitlizer extends ChannelInitializer<NioSocketChannel> {
    private TileRequestHandler tileRequestHandler;
    private TileReponsePackageHandler tileResponsePackageHandler;

    private String district;
    private TilePackageManager tilePackageManager;
    private EventExecutorGroup eventExecutors = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors() * 4);


    public ChildChannelHandlerInitlizer(String district, TilePackageManager tilePackageManager) {
        this.district = district;
        tileRequestHandler = new TileRequestHandler(this.district);
        this.tileResponsePackageHandler = new TileReponsePackageHandler();
        this.tilePackageManager = tilePackageManager;
    }

    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {
        //http
        ch.pipeline().addLast(new HttpRequestDecoder());
        ch.pipeline().addLast(new HttpObjectAggregator(8096));

//        ch.pipeline().addLast(new TileRequestHandler(district));
        ch.pipeline().addLast(tileRequestHandler);

        ch.pipeline().addLast(eventExecutors, null, new TileReadHandler(tilePackageManager));

        ch.pipeline().addLast(tileResponsePackageHandler);
        ch.pipeline().addLast(new HttpContentCompressor());
        ch.pipeline().addLast(new HttpResponseEncoder());
    }
}
