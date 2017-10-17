package com.villcore.gis.tiles.server;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class ChildChannelHandlerInitlizer extends ChannelInitializer<NioSocketChannel> {
    private TileRequestHandler tileRequestHandler;

    private String district;

    public ChildChannelHandlerInitlizer(String district) {
        this.district = district;
        tileRequestHandler = new TileRequestHandler(this.district);
    }

    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {
        //http
        ch.pipeline().addLast(new HttpRequestDecoder());
        ch.pipeline().addLast(new HttpResponseEncoder());
        ch.pipeline().addLast(new TileRequestHandler(district));
    }
}
