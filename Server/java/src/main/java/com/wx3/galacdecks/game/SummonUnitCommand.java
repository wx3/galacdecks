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
 * @author Kevin
 *
 */
public class SummonUnitCommand extends GameCommand {

	private int cardEntityId;
	private int x, y;
	
	public SummonUnitCommand() {}
	
	public SummonUnitCommand(int cardId, int x, int y) {
		this.cardEntityId = cardId;
		this.x = x;
		this.y = y;
	}
	
	@Override
	public boolean isValidCommand(PlayValidator validator, GameState game) {
		GameEntity entity = game.getEntity(cardEntityId);
		return validator.isValidSummon(entity, x, y);
	}

	/* (non-Javadoc)
	 * @see com.wx3.galacdecks.game.GameCommand#execute(com.wx3.galacdecks.game.GameRules)
	 */
	@Override
	public void execute(GameRules gameRules) {
		GameEntity card = gameRules.getEntity(cardEntityId);
		gameRules.playUnitFromHand(card, x, y);
	}

}
