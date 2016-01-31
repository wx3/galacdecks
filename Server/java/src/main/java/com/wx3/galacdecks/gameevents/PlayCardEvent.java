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
package com.wx3.galacdecks.gameevents;

import com.wx3.galacdecks.game.EntityStat;
import com.wx3.galacdecks.game.GameEntity;
import com.wx3.galacdecks.game.PlayerState;

/**
 * @author Kevin
 *
 */
public class PlayCardEvent extends GameEvent {

	public transient GameEntity card;
	public transient GameEntity target;
	public int x, y;
	public int energyCost, mineralCost;
	
	private int cardId;
	
	public PlayCardEvent(GameEntity card, int col, int row) {
		this(card, col, row, null);
	}
	
	public PlayCardEvent(GameEntity card, int col, int row, GameEntity target) {
		this.card = card;
		this.cardId = card.getId();
		this.x = col;
		this.y = row;
		this.energyCost = card.getStat(EntityStat.ENERGY_COST);
		this.mineralCost = card.getStat(EntityStat.MINERAL_COST);
		this.prototypeName = "Play Card Event";
		this.target = target;
	}
	
	@Override
	public EventView getPlayerView(PlayerState viewer) {
		if(viewer == card.getOwner()) {
			// not sure I like this here:
			return super.getPlayerView(viewer);	
		} else {
			return new OpponentPlayCardView(viewer, card);
		}
		
	}

}
