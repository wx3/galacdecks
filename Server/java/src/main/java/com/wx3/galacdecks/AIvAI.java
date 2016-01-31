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

import com.wx3.galacdecks.ai.*;
import com.wx3.galacdecks.datastore.GameDatastore;
import com.wx3.galacdecks.game.GameInstance;
import com.wx3.galacdecks.game.GameSystem;
import com.wx3.galacdecks.game.PlayerInstance;

/**
 * Creates 1 or more games where both sides are played by an AI,
 * useful for both stress testing and uncovering bugs.
 * 
 * @author Kevin
 *
 */
public class AIvAI {

	/**
	 * Entry point for running AIvAI game tests.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		GameDatastore datastore = new GameDatastore();
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.init(datastore, "data");
		AiManager aiManager = new AiManager(0.001f);
		aiManager.start();
		
		for(int i = 0; i < 1; i++) {
			GameSystem gameSystem = datastore.getSystem("HIVE");
			GameInstance game = new GameInstance(gameSystem);
			PlayerInstance p1 = new PlayerInstance(game.getGameId(), "AI_1", 1, 1);

			GameAI ai1 = new EvaluatorAI(p1);
			p1.connect(ai1, game);
			game.join(p1);
			aiManager.registerAI(ai1);
			
			PlayerInstance p2 = new PlayerInstance(game.getGameId(), "AI_2", 2, 2);
			
			GameAI ai2 = new EvaluatorAI(p2);
			p2.connect(ai2, game);
			game.join(p2);
			
			aiManager.registerAI(ai2);
			game.start();
		}
	}

}
