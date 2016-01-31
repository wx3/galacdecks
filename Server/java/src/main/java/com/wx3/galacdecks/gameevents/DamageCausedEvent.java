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

import java.util.ArrayList;
import java.util.List;

import com.wx3.galacdecks.game.DamageEffect;
import com.wx3.galacdecks.game.GameEntity;

/**
 * GameEvent for an entity causing damage to one or more entities
 * 
 * @author Kevin
 *
 */
@SuppressWarnings("unused")
public class DamageCausedEvent extends GameEvent {
	
	private int attackerId;
	private List<DamageEffect> damages;
	
	public DamageCausedEvent(GameEntity cause, List<DamageEffect> damages, String prefab) {
		this.attackerId = cause.getId();
		this.damages = damages;
		this.prototypeName = prefab;
	}
	
	public DamageCausedEvent(GameEntity cause, DamageEffect damage, String prefab) {
		this.attackerId = cause.getId();
		this.damages = new ArrayList<DamageEffect>();
		damages.add(damage);
		this.prototypeName = prefab;
	}
}
