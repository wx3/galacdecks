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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The representation of a player's in-game state, including
 * everything that needs to be copied to run simulations
 * on game state.
 * 
 * @author Kevin
 *
 */
public class PlayerState {

	String name;
	int position;
	int homeworldId;
	String shipPrefab;
	// Has this player's deck been initialized? 
	private boolean deckInit;
	List<GameEntity> deck = new ArrayList<GameEntity>();
	public List<GameEntity> hand = new ArrayList<GameEntity>();
	private Map<EntityStat, Integer> resources = new HashMap<EntityStat, Integer>();
	private Map<EntityStat, Integer> maxResources = new HashMap<EntityStat, Integer>();
	
	/**
	 * Verify that two player states are equivalent, but not referencing the same objects.
	 * @param p1
	 * @param p2
	 * @return
	 */
	static boolean separateButEqual(PlayerState p1, PlayerState p2) {
		if(!p1.name.equals(p2.name)) return false;
		if(p1.position != p2.position) return false;
		if(p1.homeworldId != p2.homeworldId) return false;
		if(!GameEntity.separateButEqual(p1.deck, p2.deck)) return false;
		if(!GameEntity.separateButEqual(p1.hand, p2.hand)) return false;
		return true;
	}
	
	public PlayerState(int position) {
		this.position = position;
	}
	
	PlayerState(PlayerState original) {
		name = original.name;
		position = original.position;
		homeworldId = original.homeworldId;
		for(GameEntity entity : original.deck) {
			deck.add(new GameEntity(entity, this));
		}
		for(GameEntity entity : original.hand) {
			hand.add(new GameEntity(entity, this));
		}
		resources = new HashMap<>(original.resources);
	}
	
	public String getName() {
		return name;
	}
	
	void setName(String name) {
		this.name = name;
	}
	
	public int getPosition() {
		return position;
	}
	
	public void setPosition(int pos) {
		this.position = pos;
	}
	
	public int getHomeworldId() {
		return homeworldId;
	}
	
	public void setHomeWorld(GameEntity homeworld) {
		this.homeworldId = homeworld.getId();
	}

	public String getShipPrefab() {
		return shipPrefab;
	}
	
	public void setShipPrefab(String prefabName) {
		this.shipPrefab = prefabName;
	}
	
	boolean isDeckInitialized() {
		return deckInit;
	}
	
	void setDeck(List<GameEntity> deck) {
		this.deck = deck;
		deckInit = true;
	}
	
	public int getDeckSize() {
		return deck.size();
	}
	
	public int getHandSize() {
		return hand.size();
	}
	
	public int getResource(EntityStat resource) {
		if(resources.containsKey(resource)) {
			return resources.get(resource);
		}
		return 0;
	}
	
	void setResource(EntityStat resource, int value) {
		resources.put(resource, value);
	}
	
	int getMaxResource(EntityStat resource) {
		if(maxResources.containsKey(resource)) {
			return maxResources.get(resource);
		}
		return 0;
	}
	
	void setMaxResource(EntityStat resource, int value) {
		maxResources.put(resource, value);
	}
	
	@Override
	public String toString() {
		return name + " [" + position + "]";
	}
}
