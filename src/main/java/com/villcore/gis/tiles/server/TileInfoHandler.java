package com.villcore.gis.tiles.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class TileInfoHandler extends SimpleChannelInboundHandler<TileInfo> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TileInfo tileInfo) throws Exception {

    }
}
