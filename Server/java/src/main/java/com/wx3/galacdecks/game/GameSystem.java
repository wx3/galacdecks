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

import java.util.List;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

/**
 * A star system that defines the starting state for a game
 * 
 * @author Kevin
 *
 */
@Entity
 @Table(name="game_systems")
public class GameSystem {
	
	@Id
	public String id;
	public String name;
	public String description;
	public boolean usePlayerDecks = true;
	public boolean pvp = false;
	// If this system has pre-configured ships:
	public String p1Ship;
	public String p2Ship;
	
	@ElementCollection
	@CollectionTable(name="root_rules")
	@OrderColumn(name="ruleOrder")
	// The id of rules to be attached to the root entity:
	public List<String> rootRules;
	
	// The prototypes that can be created mapped by their id:
	private transient Map<String, EntityPrototype> prototypes;
	// The rules that can be added to entities mapped by their id:
	private transient Map<String, EntityRule> rules;

	private transient Map<String, PlayerShip> playerShips;
	
	/**
	 * @return the prototypes
	 */
	public Map<String, EntityPrototype> getPrototypes() {
		return prototypes;
	}
	/**
	 * @param prototypes the prototypes to set
	 */
	public void setPrototypes(Map<String, EntityPrototype> prototypes) {
		this.prototypes = prototypes;
	}
	/**
	 * @return the rules
	 */
	public Map<String, EntityRule> getRules() {
		return rules;
	}
	/**
	 * @param rules the rules to set
	 */
	public void setRules(Map<String, EntityRule> rules) {
		this.rules = rules;
	}
	
	public PlayerShip getShip(String id) {
		if(playerShips.containsKey(id)) {
			return playerShips.get(id);
		}
		return null;
	}
	
	public Map<String, PlayerShip> getPlayerShips() {
		return playerShips;
	}
	public void setPlayerShips(Map<String, PlayerShip> playerShips) {
		this.playerShips = playerShips;
	}
	

	@Override
	public String toString() {
		return "GameSystem." + id;
	}
	
}
