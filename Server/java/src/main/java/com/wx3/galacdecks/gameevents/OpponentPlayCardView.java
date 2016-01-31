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
 * The event view sent to the client for opponent card plays.
 * The information is the same, but a different prototype is
 * used on the client to reveal the card before removal.
 * 
 * @author Kevin
 *
 */
public class OpponentPlayCardView extends EventView {
	
	public static final String PROTOTYPE = "Opponent Play Card Event";
	
	@SuppressWarnings("unused")
	private EntityView cardView;
	
	public OpponentPlayCardView(PlayerState viewer, GameEntity card) {
		eventClass = this.getClass().getSimpleName();
		prototypeName = PROTOTYPE;
		this.cardView = new EntityView(card, viewer);
	}
}
