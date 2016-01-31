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
package com.wx3.galacdecks.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wx3.galacdecks.game.GameCommand;
import com.wx3.galacdecks.game.GameRules;
import com.wx3.galacdecks.game.PlayerInstance;
import com.wx3.galacdecks.server.MessageHandler;
import com.wx3.galacdecks.server.OutboundMessage;

/**
 * Abstract base class for game AIs.
 * 
 * @author Kevin
 *
 */
public abstract class GameAI implements MessageHandler {
	
	final Logger logger = LoggerFactory.getLogger(GameAI.class);
	
	protected PlayerInstance player;
	
	protected class CommandSelection {
		
		protected GameCommand command;
		public double value;
		
		public CommandSelection(GameCommand command, double value) {
			this.command = command;
			this.value = value;
		}
		
		public GameCommand getCommand() {
			return command;
		}
	}
	
	protected boolean isGameOver() {
		GameRules game = player.getGameInstance().getGameRules();
		if(game != null) {
			return game.getGameState().isGameOver();
		}
		return false;
	}
	
	protected boolean isPlayerTurn() {
		GameRules game = player.getGameInstance().getGameRules();
		if(game == null) return false;
		if(!game.isStarted()) return false;
		if(game.getGameState().getCurrentPlayer() == game.getPlayerState(player.getPosition())) {
			return true;
		}
		return false;
	}
	
	public void update() {
		if(isPlayerTurn()) {
			logger.info("AI choosing command for " + player);
			GameCommand command = getBestCommand();
			logger.info("AI chose " + command + " for " + player);
			command.setPlayer(player);
			player.handleCommand(command);
		}
	}
	
	public abstract GameCommand getBestCommand();
	
	@Override
	public void disconnect() {}

	@Override
	public void handleMessage(OutboundMessage message) {}

}
