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



import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wx3.galacdecks.game.GameCommand;
import com.wx3.galacdecks.game.PlayerInstance;
import com.wx3.galacdecks.server.GameServer;
import com.wx3.galacdecks.server.MessageHandler;
import com.wx3.galacdecks.server.OutboundMessage;
import com.wx3.galacdecks.server.RequestHandler;

/**
 * Once a client has completed the handshake upgrading the HTTP connection to a 
 * websocket, this handler will accept JSON commands. Until a player is 
 * authenticated, these commands will be interpreted as either a Join request 
 * or passed along to the {@link GameServer}.
 * <p>
 * Once a player is authenticated, they will be translated into
 * {@link GameCommand}s by the GameServer.
 * 
 * @author Kevin
 *
 */
public class WebsocketHandler extends SimpleChannelInboundHandler<Object> implements MessageHandler {

	final Logger logger = LoggerFactory.getLogger(WebsocketHandler.class);
	
	private Channel channel;
	private RequestHandler requestHandler;
	
	public WebsocketHandler(Channel channel, GameServer gameServer) {
		this.requestHandler = new RequestHandler(gameServer, this);
		this.channel = channel;
	}
	
	public void disconnect() {
		logger.info("Disconnecting");
		if(requestHandler != null) {
			requestHandler.disconnect();
			requestHandler = null;
		}
		// To avoid an error on the client, send a close frame:
		channel.writeAndFlush(new CloseWebSocketFrame());
		channel.close();
	}

	public void handleMessage(OutboundMessage message) {
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		String encoded = gson.toJson(message);
		this.channel.writeAndFlush(new TextWebSocketFrame(encoded));
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof WebSocketFrame) {
			WebSocketFrame frame = (WebSocketFrame) msg;
	        if (frame instanceof CloseWebSocketFrame) {
	        	ctx.close();
	            return;
	        }
	        if (frame instanceof PingWebSocketFrame) {
	            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
	            return;
	        }
	        if (!(frame instanceof TextWebSocketFrame)) {
	            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
	                    .getName()));
	        }
	        String text = ((TextWebSocketFrame) frame).text();
	        JsonParser parser = new JsonParser();
     		JsonElement root = parser.parse(text);
     		JsonObject json = root.getAsJsonObject();
     		requestHandler.handleJsonRequest(json);
        } else {
        	logger.warn("Message was not WebSocketFrame.");
        	ctx.close();
        }
	}
    
    /*
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.warn("Exception caught: " + cause.getStackTrace());
        disconnect();
    }
    */
}
