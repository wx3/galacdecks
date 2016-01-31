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
package com.wx3.galacdecks.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.wx3.galacdecks.Bootstrap;
import com.wx3.galacdecks.datastore.GameDatastore;
import com.wx3.galacdecks.server.MockMessageHandler;

import junit.framework.TestCase;

/**
 * Base class for tests that require a datastore and players for
 * game creation.
 * 
 * @author Kevin
 *
 */
public abstract class GameTest extends TestCase {

	protected GameDatastore datastore;
	protected PlayerInstance p1; 
	protected PlayerInstance p2;
	protected MockMessageHandler handler1 = new MockMessageHandler();
	protected MockMessageHandler handler2 = new MockMessageHandler();
	
	protected void setUp() {
		datastore = new GameDatastore();
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.init(datastore, "data");
		p1 = new PlayerInstance(1, "goodguy", 1, 1);
		p2 = new PlayerInstance(1, "badguy", 2, 2);
	}
	
	protected GameInstance createGame(int id) {
		GameInstance game = new GameInstance(datastore.getSystem("PVP_1"));
		p1.connect(handler1, game);
		game.join(p1);
		p2.connect(handler2, game);
		game.join(p2);
		return game;
	}
	
}
