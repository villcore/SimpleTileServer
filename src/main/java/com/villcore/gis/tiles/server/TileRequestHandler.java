package com.villcore.gis.tiles.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sun.deploy.net.HttpRequest.CONTENT_LENGTH;
import static com.sun.deploy.net.HttpRequest.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@ChannelHandler.Sharable
public class TileRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TileRequestHandler.class);

    private String district;

    public TileRequestHandler(String district) {
        this.district = district;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        String url = msg.uri();
        //ctx.write(url);
//        ctx.write(new DefaultHttpResponse(HTTP_1_1,
//                HttpResponseStatus.BAD_REQUEST));

//        System.out.println(url);
//        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK,
//                Unpooled.wrappedBuffer("OK OK OK OK".getBytes()));
//
//        response.headers().set(CONTENT_TYPE, "text/plain");
//        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
//        response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
//        ctx.pipeline().writeAndFlush(response);

        try {
            if(!url.contains("map_tiles")) {
                ctx.fireChannelRead(InvalidRequest.INSTANCE);
                return;
            }
            TileInfo tileInfo = parseTileInfo(url);

            if (correctTile(tileInfo)) {
                ctx.fireChannelRead(tileInfo);
            } else {
                ctx.fireChannelRead(InvalidRequest.INSTANCE);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            ctx.fireChannelRead(InvalidRequest.INSTANCE);
        }
    }

//    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        ctx.flush();
//    }

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
        LOGGER.debug(url);
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
