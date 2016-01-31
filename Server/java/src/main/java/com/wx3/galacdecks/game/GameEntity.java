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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.event.internal.AbstractSaveEventListener.EntityState;

/**
 * All persistent objects in the game are represented by
 * a GameEntity.
 * 
 * @author Kevin
 *
 */
public class GameEntity {
	
	private int id;
	private String name;
	private String prototypeId;
	private PlayerState owner;
	private EntityCoordinates coordinates = new EntityCoordinates(-1, -1);
	private Set<EntityTag> tags = new HashSet<EntityTag>();
	private EntityStats stats = new EntityStats();
	private List<EntityRule> rules = new ArrayList<EntityRule>();
	
	/** 
	 * Check that two entities are equivalent, but not the same object (or containing the same objects)
	 * 
	 * @param e1
	 * @param e2
	 * @return
	 */
	static boolean separateButEqual(GameEntity e1, GameEntity e2) {
		if(e1 == null || e2 == null) {
			return false;
		}
		if(e1 == e2) {
			return false;
		}
		if(e1.getName() != null) {
			if(!e1.getName().equals(e2.getName())) {
				return false;
			}
		} else {
			if(e2.getName() != null) {
				return false;
			}
		}
		if(e1.getPrototypeId() != null) {
			if(!e1.getPrototypeId().equals(e2.getPrototypeId())) {
				return false;
			}	
		} else {
			if(e2.getPrototypeId() != null) {
				return false;
			}
		}
		if(e1.getOwner() != null) {
			if(e1.getOwner() == e2.getOwner()) {
				return false;
			}	
		} else {
			if(e2.getOwner() != null) {
				return false;
			}
		}
		if(e1.getY() != e2.getY()) {
			return false;
		}
		if(e1.getX() != e2.getX()) {
			return false;
		}
		for(EntityTag tag : EntityTag.values()) {
			if(e1.hasTag(tag) && !e2.hasTag(tag)) {
				return false;
			}
			if(!e1.hasTag(tag) && e2.hasTag(tag)) {
				return false;
			}
		}
		for(EntityStat stat : EntityStat.values()) {
			if(e1.getStat(stat) != e2.getStat(stat)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Similar to single entity version, but compares lists.
	 * 
	 * @param list1
	 * @param list2
	 * @return
	 */
	static boolean separateButEqual(List<GameEntity> entities1, List<GameEntity> entities2) {
		if(entities1 == entities2) return false;
		if(entities1.size() != entities2.size()) return false;
		for(int i = 0; i < entities1.size(); i++) {
			if(!separateButEqual(entities1.get(i), entities2.get(i))) return false;
		}
		return true;
	}
	
	GameEntity(int id) {
		this.id = id;
	}
	
	/**
	 * Construct entity based on a prototype
	 * @param prototype
	 */
	GameEntity(int id, EntityPrototype prototype) {
		this.id = id;
		this.name = prototype.getName();
		this.prototypeId = prototype.getId();
		this.tags = new HashSet<EntityTag>(prototype.getTags());
		this.stats = new EntityStats(prototype);
		this.rules = new ArrayList<>(prototype.getRules());
		setCurrentHealth(getMaxHealth());
		rechargeShields(getMaxShields());
	}

	/**
	 * Copy constructor
	 * @param original
	 */
	GameEntity(GameEntity original, PlayerState newOwner) {
		this.id = original.id;
		this.name = original.name;
		this.owner = newOwner;
		this.coordinates = original.coordinates;
		this.prototypeId = original.prototypeId;
		this.tags = new HashSet<EntityTag>(original.tags);
		this.stats = new EntityStats(original.stats);
		this.rules = new ArrayList<>(original.rules);
	}
	
	public PlayerState getOwner() {
		return owner;
	}
	
	public void setOwner(PlayerState owner) {
		this.owner = owner;
	}
	
	public boolean enemyOf(PlayerState owner) {
		if(owner == null) return false;
		if(this.owner == owner) return false;
		return true;
	}
	
	public boolean enemyOf(GameEntity other) {
		return enemyOf(other.getOwner());
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPrototypeId() {
		return prototypeId;
	}
	
	public boolean inDeck() {
		return tags.contains(EntityTag.IN_DECK);
	}
	
	public boolean inHand() {
		return tags.contains(EntityTag.IN_HAND);
	}
	
	public boolean inPlay() {
		return tags.contains(EntityTag.IN_PLAY);
	}
	
	public boolean isUnit() {
		return tags.contains(EntityTag.UNIT);
	}
	
	public boolean isGateway() {
		if(isDisabled()) return false;
		return tags.contains(EntityTag.GATEWAY);
	}
	
	public boolean canAttack() {
		if(!tags.contains(EntityTag.IN_PLAY)) return false;
		if(!tags.contains(EntityTag.UNIT)) return false;
		if(getAttack() < 1) return false;
		if(getActionsRemaining() < 1) return false;
		if(isDisabled()) return false;
		return true;
	}
	
	public boolean canMove() {
		if(!tags.contains(EntityTag.IN_PLAY)) return false;
		if(!tags.contains(EntityTag.UNIT)) return false;
		if(!tags.contains(EntityTag.MOVABLE)) return false;
		if(getMovesRemaining() < 1) return false;
		if(isDisabled()) return false;
		return true;
	}
	
	public boolean canDiscard() {
		if(!tags.contains(EntityTag.DISCARD_TARGET)) return false;
		if(getDiscardsRemaining() < 1) return false;
		if(isDisabled()) return false;
		return true;
	}
	
	public boolean onBoard() {
		if(coordinates.x >= 0) return true;
		return false;
	}
	
	public boolean isPower() {
		return tags.contains(EntityTag.POWER);
	}
	
	public boolean isRemoved() {
		return tags.contains(EntityTag.REMOVED);
	}
	
	public int getY() {
		return coordinates.y;
	}
	
	public int getX() {
		return coordinates.x;
	}
	
	void setPosition(EntityCoordinates coord) {
		this.coordinates = coord;
	}
	
	public EntityCoordinates getCoordinates() {
		return this.coordinates;
	}
	
	void setTag(EntityTag tag) {
		tags.add(tag);
	}
	
	void clearTag(EntityTag tag) {
		tags.remove(tag);
	}
	
	public boolean hasTag(EntityTag tag) {
		return tags.contains(tag);
	}
	
	public boolean hasTag(String tagName) {
		EntityTag tag = EntityTag.valueOf(tagName);
		return hasTag(tag);
	}
	
	public Set<EntityTag> getTags() {
		return tags;
	}
	
	public int getStat(EntityStat stat) {
		return stats.getCurrent(stat);
	}
	
	public int getStat(String statName) {
		return getStat(EntityStat.valueOf(statName));
	}
	
	public Map<EntityStat, Integer> getStats() {
		return stats.getCurrentStats();
	}
	
	public void buffStat(EntityStat stat, int amount) {
		stats.buff(stat, amount);
	}
	
	public void buffStat(String statName, int amount) {
		EntityStat stat = EntityStat.valueOf(statName);
		buffStat(stat, amount);
	}
	
	public int getMaxHealth() {
		return stats.getCurrent(EntityStat.MAX_HEALTH);
	}
	
	public int getCurrentHealth() {
		return stats.getCurrent(EntityStat.CURRENT_HEALTH);
	}
	
	public int getAttack() {
		return stats.getCurrent(EntityStat.ATTACK);
	}
	
	public void increaseMaxEnergy(int amount) {
		int current = stats.getBase(EntityStat.ENERGY_RESOURCE);
		stats.setBase(EntityStat.ENERGY_RESOURCE, current + amount);
	}
	
	public void increaseMaxMineral(int amount) {
		int current = stats.getBase(EntityStat.MINERAL_RESOURCE);
		stats.setBase(EntityStat.MINERAL_RESOURCE, current + amount);
	}
	
	void addRule(EntityRule rule) {
		rules.add(rule);
	}
	
	List<EntityRule> getRules() {
		return rules;
	}
	
	int damage(int amount) {
		if(amount > getCurrentHealth()) amount = getCurrentHealth();
		setCurrentHealth(getCurrentHealth() - amount);
		return amount;
	}
	
	void repair(int amount) {
		int health = getCurrentHealth();
		health += amount;
		setCurrentHealth(health);
	}
	
	public void setCurrentHealth(int val) {
		if(val > getMaxHealth()) {
			stats.setValue(EntityStat.CURRENT_HEALTH, getMaxHealth());
		} else {
			stats.setValue(EntityStat.CURRENT_HEALTH, val);
		}
	}
	
	/***
	 * Disable the entity for turns (typically 2, which means until the 
	 * start of the unit's next turn).
	 * @param turns
	 */
	public void disable(int turns) {
		stats.adjustValue(EntityStat.DISABLED, turns);
	}
	
	public boolean isDisabled() {
		if(stats.getCurrent(EntityStat.DISABLED) > 0) return true;
		return false;
	}
	
	public int getCurrentShields() {
		return stats.getCurrent(EntityStat.CURRENT_SHIELDS);
	}
	
	public int getMaxShields() {
		return stats.getCurrent(EntityStat.MAX_SHIELDS);
	}
	
	void dischargeShields(int amount) {
		stats.adjustValue(EntityStat.CURRENT_SHIELDS, -amount);
	}
	
	int rechargeShields(int amount) {
		int current = getCurrentShields();
		if(current + amount > stats.getCurrent(EntityStat.MAX_SHIELDS)) {
			amount = stats.getCurrent(EntityStat.MAX_SHIELDS) - current;
		}
		current += amount;
		stats.setValue(EntityStat.CURRENT_SHIELDS, current);
		return amount;
	}
	
	public int getMovesRemaining() {
		return stats.getCurrent(EntityStat.MOVES_REMAINING);
	}
	
	public void useMove() {
		int remaining = getMovesRemaining();
		if(remaining < 1) {
			throw new RuntimeException("No moves remaining");
		}
		stats.setValue(EntityStat.MOVES_REMAINING, remaining - 1);
	}
	
	public int getActionsRemaining() {
		return stats.getCurrent(EntityStat.ACTIONS_REMAINING);
	}
	
	public void useAction() {
		if(getActionsRemaining() < 1) {
			throw new RuntimeException("No actions remaining");
		}
		stats.setValue(EntityStat.ACTIONS_REMAINING, getActionsRemaining() - 1);
	}
	
	public int getDiscardsRemaining() {
		return stats.getCurrent(EntityStat.DISCARDS_REMAINING);
	}
	
	public void useDiscard() {
		if(getDiscardsRemaining() < 1) {
			throw new RuntimeException("No discards remaining");
		}
		stats.setValue(EntityStat.DISCARDS_REMAINING, getDiscardsRemaining() - 1);
	}
	
	/**
	 * Reset the entity for the start of the turn (refresh remaining moves & actions)
	 */
	public void turnReset() {
		int actions = 1 + stats.getCurrent(EntityStat.EXTRA_ACTIONS);
		stats.setValue(EntityStat.ACTIONS_REMAINING, actions);
		int moves = 1 + stats.getCurrent(EntityStat.EXTRA_MOVES);
		stats.setValue(EntityStat.MOVES_REMAINING, moves);
		if(hasTag(EntityTag.DISCARD_TARGET)) {
			stats.setValue(EntityStat.DISCARDS_REMAINING, 1);	
		}
	}
	
	public void deductDisable() {
		int disabled = stats.getCurrent(EntityStat.DISABLED);
		if(disabled > 0) --disabled;
		stats.setValue(EntityStat.DISABLED, disabled);
	}
	
	public int getRange() {
		return stats.getCurrent(EntityStat.RANGE_BONUS) + 1;
	}
	
	/**
	 * How much energy does this entity give the player?
	 * @return
	 */
	public int getEnergyResource() {
		return stats.getCurrent(EntityStat.ENERGY_RESOURCE);
	}
	
	/**
	 * How much minerals does this entity give the player?
	 * @return
	 */
	public int getMineralResource() {
		return stats.getCurrent(EntityStat.MINERAL_RESOURCE);
	}
	
	public int getEnergyCost() {
		return stats.getCurrent(EntityStat.ENERGY_COST);
	}
	
	public int getMineralCost() {
		return stats.getCurrent(EntityStat.MINERAL_COST);
	}
	
	void resetStats() {
		stats.resetForBuff();
	}
	
	@Override
	public String toString() {
		return name + "." + id + '@' + Integer.toHexString(hashCode());
	}
	
}
