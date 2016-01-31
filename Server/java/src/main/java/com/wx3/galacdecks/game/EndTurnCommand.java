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


/**
 * Command to end the player's turn, containing choices
 * for end of turn boosts
 * 
 * @author Kevin
 *
 */
@SuppressWarnings("unused")
public class EndTurnCommand extends GameCommand {
	
	private boolean energy;
	private boolean mineral;
	private boolean draw;
	
	public EndTurnCommand() {}
	
	public EndTurnCommand(boolean energy, boolean mineral, boolean draw) {
		this.energy = energy;
		this.mineral = mineral;
		this.draw = draw;
	}

	/**
	 * @return the energy
	 */
	public boolean isEnergy() {
		return energy;
	}

	/**
	 * @return the mineral
	 */
	public boolean isMineral() {
		return mineral;
	}

	/**
	 * @return the draw
	 */
	public boolean isDraw() {
		return draw;
	}

	@Override
	public boolean isValidCommand(PlayValidator validator, GameState game) {
		if(game.getCurrentPlayer().getPosition() == playerPosition) return true;
		return false;
	}

	@Override
	public void execute(GameRules gameRules) {
		PlayerState playerState = gameRules.getPlayerState(playerPosition);
		GameEntity homeworld = gameRules.getHomeworld(playerState);
		if(homeworld == null) {
			throw new RuntimeException("Player is missing homeworld");
		}
		int count = 0;
		if(isEnergy()) ++count;
		if(isMineral()) ++count;
		if(isDraw()) ++count;
		if(count != 1) {
			throw new RuntimeException("Expected exactly 1 end turn choice");
		}
		if(isEnergy()) {
			homeworld.increaseMaxEnergy(1);
		}
		if(isMineral()) {
			homeworld.increaseMaxMineral(1);
		}
		if(isDraw()) {
			gameRules.playerDrawCard(playerState);
			gameRules.playerDrawCard(playerState);
		}
		gameRules.endTurn();
		gameRules.processEvents();
		gameRules.startTurn();
	}

}
