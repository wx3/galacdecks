/**
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
 * 
 */
/**
 * 
 */
package com.wx3.galacdecks;

import com.wx3.galacdecks.datastore.GameDatastore;
import com.wx3.galacdecks.networking.NettyWebSocketServer;
import com.wx3.galacdecks.server.GameServer;

/**
 * The main entry point for the server application.
 * 
 * @author Kevin
 *
 */
public class GalacDecks {
	
	public static final int SERVER_VERSION = 8;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GameDatastore datastore = new GameDatastore();
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.init(datastore, "data");
		GameServer gameServer = new GameServer(datastore);
		NettyWebSocketServer socketServer = new NettyWebSocketServer(gameServer, 8080);
		gameServer.start(0.1f);
		socketServer.start();
	}
	
}
