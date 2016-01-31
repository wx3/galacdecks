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
package com.wx3.galacdecks.server;

import java.util.List;

import com.wx3.galacdecks.Bootstrap;
import com.wx3.galacdecks.datastore.GameDatastore;
import com.wx3.galacdecks.game.GameInstance;
import com.wx3.galacdecks.game.PlayerInstance;
import com.wx3.galacdecks.game.messages.GameInitMessage;
import com.wx3.galacdecks.server.GameServer;

import junit.framework.TestCase;

/**
 * @author Kevin
 *
 */
public class GameServerTest extends TestCase {

	private GameDatastore datastore;
	private User user1;
	private User user2;
	
	protected void setUp() {
		datastore = new GameDatastore();
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.init(datastore, "data");
		user1 = User.CreateUser("goodguy");
		user2 = User.CreateUser("badguy");
	}

}
