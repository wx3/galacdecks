/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.wx3.galacdecks.networking;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;

/**
 * Netty uses a pipeline model to process messages, with successive handlers processing
 * the results of the previous handler. Here we define the initial pipeline to decode
 * HTTP requests using existing Netty handlers and finally process that request with
 * our own {@link HttpHandler}. 
 */
public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;
    private NettyWebSocketServer server;

    public WebSocketServerInitializer(NettyWebSocketServer server, SslContext sslCtx) {
        this.sslCtx = sslCtx;
        this.server = server;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new HttpHandler(server));
    }
}
