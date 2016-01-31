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

/**
 * A Player's view of a card draw.
 * 
 * @author Kevin
 *
 */
@SuppressWarnings("unused")
public class DrawCardView extends EventView {

	private String player;
	private EntityView card;
	private int deckSize;
	
	public DrawCardView(String player, EntityView card, String prototype, int deckSize) {
		this.player = player;
		this.card = card;
		this.prototypeName = prototype;
		this.deckSize = deckSize;
	}
}