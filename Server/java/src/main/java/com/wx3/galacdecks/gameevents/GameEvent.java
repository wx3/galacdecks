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
package com.wx3.galacdecks.gameevents;

import com.wx3.galacdecks.game.EntityRule;
import com.wx3.galacdecks.game.GameEntity;
import com.wx3.galacdecks.game.PlayerState;

/**
 * GameEvents are the observable effects of actions and rules. 
 * They can trigger {@link EntityRule} firings. They are also
 * transmitted to clients so they may update their model of 
 * the game state.  
 * 
 * @author Kevin
 *
 */
public abstract class GameEvent extends EventView {

	/**
	 * If a GameEvent was caused by another entity (such as a rule
	 * firing), the cause will reference that entity.
	 */
	protected transient GameEntity cause;
	protected int causeId;
	
	/**
	 * By default, GameEvents are simply encoded as JSON and sent to
	 * the player. But some events may use a different behavior, 
	 * for example when drawing a card, the other player will not
	 * know what card was drawn.
	 * 
	 * @param viewer
	 * @return
	 */
	public EventView getPlayerView(PlayerState viewer) {
		return this;
	}
	
}
