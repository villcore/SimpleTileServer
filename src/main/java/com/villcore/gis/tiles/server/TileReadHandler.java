package com.villcore.gis.tiles.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class TileReadHandler extends SimpleChannelInboundHandler<TileInfo>{
    private TilePackageManager tilePackageManager;

    public TileReadHandler(TilePackageManager tilePackageManager) {
        this.tilePackageManager = tilePackageManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TileInfo msg) throws Exception {
//        System.out.println(tilePackageManager);
//        System.out.println(msg);

        ByteBuf byteBuf = tilePackageManager.getTileToByteBuf(msg.getzLevel(), msg.getxLevel(), msg.getyLevel());
        System.out.println(byteBuf.capacity());
        ctx.fireChannelRead(byteBuf.retain());
    }
}
