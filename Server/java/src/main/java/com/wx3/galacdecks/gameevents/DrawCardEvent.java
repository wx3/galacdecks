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

import com.wx3.galacdecks.game.EntityView;
import com.wx3.galacdecks.game.GameEntity;
import com.wx3.galacdecks.game.PlayerState;

/**
 * @author Kevin
 *
 */
public class DrawCardEvent extends GameEvent {
	
	public static final String PLAYER_PROTOTYPE = "Draw Player Event";
	public static final String OPPONENT_PROTOTYPE = "Draw Opponent Event";
	
	public transient PlayerState drawer;
	public transient GameEntity card;

	public DrawCardEvent(PlayerState drawer, GameEntity card) {
		this.drawer = drawer;
		this.card = card;
	}
	
	@Override
	public EventView getPlayerView(PlayerState viewer) {
		EntityView ev = new EntityView(card, viewer);
		String prototype;
		if(viewer == drawer) {
			prototype = PLAYER_PROTOTYPE;
		} else {
			prototype = OPPONENT_PROTOTYPE;
		}
		DrawCardView eventView = new DrawCardView(drawer.getName(), ev, prototype, drawer.getDeckSize());
		return eventView;
	}
}
