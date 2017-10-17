package com.villcore.gis.tiles.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class TileRequestHandler extends SimpleChannelInboundHandler<HttpRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TileRequestHandler.class);

    private String district;

    public TileRequestHandler(String district) {
        this.district = district;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
        String url = msg.uri();

        TileInfo tileInfo = parseTileInfo(url);

        if(correctTile(tileInfo)) {
            ctx.fireChannelRead(tileInfo);
        } else {
            ctx.fireChannelRead(InvalidRequest.INSTANCE);
        }
    }

    private boolean correctTile(TileInfo tileInfo) {
        if (tileInfo == null) {
            return false;
        }

        if(tileInfo.getDistrict() == null || !tileInfo.getDistrict().equals(district)) {
            return false;
        }

        if(tileInfo.getzLevel() < 0 || tileInfo.getzLevel() > 29) {
            return false;
        }

        if(tileInfo.getxLevel() < 0 || tileInfo.getxLevel() > Integer.MAX_VALUE) {
            return false;
        }

        if(tileInfo.getyLevel() < 0 || tileInfo.getyLevel() > Integer.MAX_VALUE) {
            return false;
        }

        return true;
    }

    private TileInfo parseTileInfo(String url) {
        //"/map_tiles/beijing/18/215933/99476.png"
        String prefix = "/map_tiles/";

        int prefixEnd = url.indexOf(prefix) + prefix.length();
        int districtEnd = url.indexOf("/", prefixEnd);
        String district = url.substring(prefixEnd, districtEnd);

        int zLevelEnd = url.indexOf("/", districtEnd + 1);
        int zLevel = Integer.valueOf(url.substring(districtEnd + 1, zLevelEnd));

        int xLevelEnd = url.indexOf("/", zLevelEnd + 1);
        int xLevel = Integer.valueOf(url.substring(zLevelEnd + 1, xLevelEnd));

        int yLevelEnd = url.indexOf(".", xLevelEnd + 1);
        int yLevel = Integer.valueOf(url.substring(xLevelEnd + 1, yLevelEnd));

        TileInfo tileInfo = new TileInfo(district, zLevel, xLevel, yLevel);
        LOGGER.debug("request tile = {}", tileInfo);

        return tileInfo;
    }
}
