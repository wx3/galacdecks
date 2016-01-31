/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Kevin Lin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *******************************************************************************/
package com.wx3.galacdecks.networking;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpHeaders.Names.UPGRADE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

/**
 * A simple webserver that listens for websocket upgrade requests, sending
 * a static default page to all other requests. 
 * <p>
 * Once a client has upgraded, this handler is replaced with a 
 * {@link WebsocketHandler}.
 */
public class HttpHandler extends SimpleChannelInboundHandler<Object> {
	
	final Logger logger = LoggerFactory.getLogger(HttpHandler.class);
	
	// Figure out where this belongs:
	public static final boolean SSL = false;

    private static final String WEBSOCKET_PATH = "/websocket";

    // The HTML webpage to send in response to a request:
    private String webpage;
    private NettyWebSocketServer server;
    private WebSocketServerHandshaker handshaker;
    
    private static void sendHttpResponse(
        ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpHeaders.setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        String location =  req.headers().get(HOST) + WEBSOCKET_PATH;
        if (SSL) {
            return "wss://" + location;
        } else {
            return "ws://" + location;
        }
    }
    
    public HttpHandler(NettyWebSocketServer server) {
    	this.server =server;
    	
    	URL url = Resources.getResource("index.html");

    	try {
			this.webpage = Resources.toString(url, Charsets.UTF_8);
		} catch (IOException e) {
			this.webpage = "No default webpage for this WebSocketServer";
		}
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else {
        	logger.warn("Message was not HttpRequest.");
        	ctx.close();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
    	logger.info("Request received...");
        // Handle a bad request.
        if (!req.getDecoderResult().isSuccess()) {
        	logger.warn("Bad request");
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // Allow only GET methods.
        if (req.getMethod() != GET) {
        	logger.warn("Unsupported method");
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }
        
        String uri = req.getUri();
        
        // If the request is for the /websocket path, expect an Upgrade header and attempt
        // the websocket handshake:
        if(uri.equals(WEBSOCKET_PATH)) {
        	String upgrade = req.headers().get(UPGRADE);
            if(upgrade == null) {
            	logger.warn("Expected upgrade");
            	sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            	return;
            }
            // Handshake
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    getWebSocketLocation(req), null, true);
            handshaker = wsFactory.newHandshaker(req);
            if (handshaker == null) {
            	logger.warn("Unsupported version");
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
            	logger.info("Switching handler to WebsocketHandler");
                handshaker.handshake(ctx.channel(), req);
                ctx.channel().pipeline().replace(HttpHandler.class, "MessageHandler", new WebsocketHandler(ctx.channel(), server.getGameServer()));
                logger.info("Handler switched.");
            }
        } else {
        	sendHttpResponse(ctx, req, webpage);
        	return;
        }
    }
    
    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, String text) {
    	ByteBuf content = Unpooled.copiedBuffer(text.getBytes()); 
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

        res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
        HttpHeaders.setContentLength(res, content.readableBytes());

        sendHttpResponse(ctx, req, res);
    }
    
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	logger.warn("Exception caught in context: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
    

}
