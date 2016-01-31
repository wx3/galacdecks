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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A collection of {@link EntityStat}s with base values and current
 * values. Buffable stats have their values recalculated after every
 * event loop, while non-buffable stats behave like named integer
 * variables. E.g., ATTACK would be a buffable stat because another
 * entity might be providing a dynamic buff, but current health would
 * not-- it only changes when the unit takes damage or is healed.
 * 
 * @author Kevin
 *
 */
public class EntityStats {

	private Map<EntityStat, Integer> baseValues = new HashMap<>();
	private Map<EntityStat, Integer> currentValues = new HashMap<>();
	
	public EntityStats() 
	{
		currentValues = new HashMap<>();
	}
	
	public EntityStats(EntityStats original) {
		this.baseValues = new HashMap<EntityStat,Integer>(original.baseValues);
		this.currentValues = new HashMap<EntityStat,Integer>(original.currentValues);
		resetForBuff();
	}
	
	public EntityStats(EntityPrototype prototype) {
		this.baseValues = new HashMap<EntityStat, Integer>(prototype.getStats());
		this.currentValues = new HashMap<>(this.baseValues);
		resetForBuff();
	}
	
	public int getCurrent(EntityStat stat) {
		if(currentValues.containsKey(stat)) {
			return currentValues.get(stat);
		}
		return 0;
	}
	
	public Map<EntityStat, Integer> getCurrentStats() {
		return Collections.unmodifiableMap(currentValues);
	}
	
	public int getBase(EntityStat stat) {
		if(baseValues.containsKey(stat)) {
			return baseValues.get(stat);
		}
		return 0;
	}
	
	public void setBase(EntityStat stat, int value) {
		if(stat.max < value) {
			value = stat.max;
		}
		baseValues.put(stat, value);
		currentValues.put(stat, value);
	}
	
	void buff(EntityStat stat, int amount) {
		if(!stat.buffable) {
			throw new RuntimeException(stat + " is not buffable");
		}
		int current = getCurrent(stat);
		current += amount;
		if(current < 0) current = 0;
		if(current > stat.max) current = stat.max;
		currentValues.put(stat, current);
	}
	
	public void adjustValue(EntityStat stat, int amount) {
		setValue(stat, getCurrent(stat) + amount);
	}
	
	/**
	 * Change the current value of a non-buffable stat, like CURRENT_HEALTH
	 * @param stat
	 * @param value
	 */
	public void setValue(EntityStat stat, int value) {
		if(stat.buffable) {
			throw new RuntimeException(stat + " is a buffable stat, modify using buff()");
		}
		if(value < 0) value = 0;
		if(value > stat.max) value = stat.max;
		currentValues.put(stat,  value);
	}
	
	/**
	 * Reset the current values of buffable stats to their base value.
	 */
	void resetForBuff() {
		for(EntityStat stat : currentValues.keySet()) {
			if(stat.buffable) {
				currentValues.put(stat, getBase(stat));
			}
		}
	}
}
