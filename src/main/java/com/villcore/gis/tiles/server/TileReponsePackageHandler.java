package com.villcore.gis.tiles.server;

import com.sun.deploy.net.HttpRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@ChannelHandler.Sharable
public class TileReponsePackageHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TileReponsePackageHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOGGER.debug("package tile to resp ...");

        if(msg instanceof ByteBuf) {
            ByteBuf tileBuf = (ByteBuf) msg;
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, tileBuf);

            response.headers().set(HttpRequest.CONTENT_TYPE, "image/png");
            response.headers().set(HttpRequest.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            response.headers().set("Access-Control-Allow-Origin", "*");
            ctx.pipeline().write(response);
            return;
        }

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST,
                Unpooled.EMPTY_BUFFER);
        response.headers().set(HttpRequest.CONTENT_LENGTH, response.content().readableBytes());
        ctx.pipeline().write(response);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
