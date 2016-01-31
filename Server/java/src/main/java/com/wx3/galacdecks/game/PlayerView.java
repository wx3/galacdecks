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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Player's view of a player, including entities in their
 * hand (details of the entities may be concealed).
 * 
 * @author Kevin
 *
 */
public class PlayerView {
	
	String playerName;
	int position;
	int deckSize;
	int homeworldId;
	String shipPrefab;
	List<EntityView> hand = new ArrayList<EntityView>();
	Map<EntityStat, Integer> currentResources = new HashMap<EntityStat, Integer>();
	Map<EntityStat, Integer> maxResources = new HashMap<>();
	
	public PlayerView(PlayerState player, PlayerState viewer) {
		playerName = player.getName();
		position = player.position;
		homeworldId = player.getHomeworldId();	
		shipPrefab = player.getShipPrefab();
		for(GameEntity entity : player.hand) {
			hand.add(new EntityView(entity, viewer));
		}
		deckSize = player.deck.size();
		currentResources.put(EntityStat.ENERGY_RESOURCE, player.getResource(EntityStat.ENERGY_RESOURCE));
		currentResources.put(EntityStat.MINERAL_RESOURCE, player.getResource(EntityStat.MINERAL_RESOURCE));
		maxResources.put(EntityStat.ENERGY_RESOURCE, player.getMaxResource(EntityStat.ENERGY_RESOURCE));
		maxResources.put(EntityStat.MINERAL_RESOURCE, player.getMaxResource(EntityStat.MINERAL_RESOURCE));
	}

}
