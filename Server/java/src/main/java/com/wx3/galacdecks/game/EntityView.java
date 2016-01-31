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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A Player's view of an entity. For many entities, this just mirrors
 * the entity data, but in some cases a player may not be able to see
 * some information, such as details about cards in the other player's 
 * hand, or cards in either player's deck.
 * 
 * An EntityView is encoded as JSON to be sent to the client.
 * 
 * @author Kevin
 *
 */
@SuppressWarnings("unused")
public class EntityView {

	boolean visible;
	int id;
	String name;
	String owner;
	String prototype;
	private int row = -1;
	private int column = -1;
	private Set<EntityTag> tags;
	private Map<EntityStat, Integer> stats;
	
	public EntityView(GameEntity entity, PlayerState viewer) {
		if(entity.inDeck()) {
			initHidden(entity);
		} else {
			if(entity.inHand()) {
				if(entity.getOwner() == viewer) {
					initVisibile(entity);
				} else {
					initHidden(entity);
				}
			} else {
				initVisibile(entity);
			}
		}
	}
	
	/**
	 * Initial the view for a player that can see this entity's details,
	 * e.g., a non-hidden entity on the board.
	 * 
	 * @param entity
	 */
	private void initVisibile(GameEntity entity) {
		visible = true;
		if(entity.getOwner() != null) {
			owner = entity.getOwner().getName();	
		}
		id = entity.getId();
		name = entity.getName();
		prototype = entity.getPrototypeId();
		row = entity.getY();
		column = entity.getX();
		tags = new HashSet<EntityTag>(entity.getTags());
		stats = entity.getStats();
	}
	
	/**
	 * Initial the view for a player that can't see this entity's details,
	 * e.g., a card in the other player's hand.
	 * 
	 * @param entity
	 */
	private void initHidden(GameEntity entity) {
		visible = false;
		if(entity.getOwner() != null) {
			owner = entity.getOwner().getName();	
		}
		id = entity.getId();
		tags = new HashSet<EntityTag>();
		// These tags are still visible for hidden entities:
		if(entity.hasTag(EntityTag.IN_HAND)) {
			tags.add(EntityTag.IN_HAND);
		}
		if(entity.hasTag(EntityTag.IN_PLAY)) {
			tags.add(EntityTag.IN_PLAY);
		}
		if(entity.hasTag(EntityTag.IN_DECK)) {
			tags.add(EntityTag.IN_DECK);
		}
	}
}
