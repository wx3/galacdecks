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

import com.wx3.galacdecks.gameevents.AttackEvent;


/**
 * @author Kevin
 *
 */
public class AttackCommand extends GameCommand {

	private int attackerId;
	private int x, y;
	
	public AttackCommand() {}
	
	public AttackCommand(int attackerId, int x, int y) {
		this.attackerId = attackerId;
		this.x = x;
		this.y = y;
	}
	
	/**
	 * @return the attackerId
	 */
	public int getAttackerId() {
		return attackerId;
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	@Override
	public boolean isValidCommand(PlayValidator validator, GameState game) {
		GameEntity attacker = game.getEntity(attackerId);
		return validator.isValidAttack(attacker, x, y);
	}

	
	@Override
	public void execute(GameRules gameRules) {
		GameEntity attacker = gameRules.getEntity(attackerId);
		GameEntity target = gameRules.getEntityAt(x, y);
		int amount = attacker.getStat(EntityStat.ATTACK);
		attacker.useAction();
		gameRules.addEvent(new AttackEvent(attacker, target, amount));
		gameRules.dealDamage(attacker, target, amount);
		
	}

}
