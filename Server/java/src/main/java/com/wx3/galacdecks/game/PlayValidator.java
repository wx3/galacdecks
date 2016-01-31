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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the valid actions for a player: cards 
 * that can be played on which target, entity 
 * moves and entity attacks.
 * 
 * This information is used both for the client
 * to hint in the UI and for the AI to enumerate
 * possible commands.
 * 
 * @author Kevin
 *
 */
public class PlayValidator {
	
	final static Logger logger = LoggerFactory.getLogger(PlayValidator.class);
	
	public static PlayValidator GenerateValidPlays(GameRules gameRules, PlayerState player) {
		GameState gameState = gameRules.getGameState();
		PlayValidator validPlays = new PlayValidator();
		// If it's not our turn, we have no valid plays:
		if(gameState.getCurrentPlayer() != player) return validPlays;
		// For all the cards in the player's hand:
		for(GameEntity entity : player.hand) {
			// Get a list of valid discard target locations for the card:
			validPlays.validDiscards.put(entity.getId(), GetDiscardTargets(gameRules, entity));
			// Next, see if we have a custom validator:
			EntityPrototype prototype = gameRules.getEntityPrototype(entity);
			if(prototype.getPlayValidator() != null) {
				if(HasResources(player, entity)) 
				{
					List<EntityCoordinates> validCoord = GetCustomValidatorTargets(gameRules, entity, prototype.getPlayValidator());
					if(entity.isUnit()) {
						validPlays.validSummons.put(entity.getId(), validCoord);
					}
					else {
						validPlays.validPowers.put(entity.getId(), validCoord);
					}
				}
			}
			else {
				if(HasResources(player, entity)) {
					// If it's a unit, see if it can be summoned:
					if(entity.isUnit()) {
						List<EntityCoordinates> validCoord = GetUnitSummons(gameRules, player, entity);
						validPlays.validSummons.put(entity.getId(), validCoord);
					}
					// If it's a power that requires no target and no custom validator, just check for resources
					if(entity.hasTag(EntityTag.NO_TARGET)) {
						validPlays.validNoTarget.add(entity.getId());
					}
				}
			}
		}

		// Get a list of valid targets for attacking units on the board:
		for(GameEntity entity : gameState.getInPlay(player)) {
			if(entity.canAttack()) {
				List<EntityCoordinates> validCoord = GetUnitAttacks(gameRules, player, entity);
				validPlays.validAttacks.put(entity.getId(), validCoord);
			}
			if(entity.canMove()) {
				List<EntityCoordinates> validCoord = GetUnitMoves(gameRules, player, entity);
				validPlays.validMoves.put(entity.getId(), validCoord);
			}
		}
		return validPlays;
	}
	
	private static List<EntityCoordinates> GetCustomValidatorTargets(GameRules gameRules, GameEntity entity, ValidatorScript validator) {
		GameState gameState = gameRules.getGameState();
		List<EntityCoordinates> coords = new ArrayList<EntityCoordinates>();
		for(EntityCoordinates coord : gameState.getAllCoordinates()) {
			if(gameRules.validatePlayCard(entity, coord, validator)) {
				coords.add(coord);
			}
		}
		return coords;
	}
	
	private static List<EntityCoordinates> GetDiscardTargets(GameRules gameRules, GameEntity card) {
		GameState gameState = gameRules.getGameState();
		List<EntityCoordinates> coords = new ArrayList<EntityCoordinates>();
		List<GameEntity> discardTargets = gameState.getInPlayWithTag(EntityTag.DISCARD_TARGET);
		for(GameEntity candidate : discardTargets) {
			if(candidate.canDiscard()) {
				EntityPrototype prototype = gameRules.getEntityPrototype(candidate);
				ValidatorScript validator = prototype.getDiscardValidator();
				if(validator == null) {
					logger.warn("Discard target didn't have discard validator.");
				}
				if(gameRules.validateDiscard(card, candidate, validator)) {
					coords.add(candidate.getCoordinates());
				}
			}
		}
		return coords;
	}
	
	
	// Current rules: by default a player can summon a unit next to any friendly unit with the GATEWAY tag.
	private static List<EntityCoordinates> GetUnitSummons(GameRules gameRules, PlayerState player, GameEntity entity) {
		GameState gameState = gameRules.getGameState();
		List<EntityCoordinates> coords = new ArrayList<EntityCoordinates>();
		for(GameEntity playerEntity : gameState.getInPlay(player)) {
			if(playerEntity.isGateway()) {
				for(EntityCoordinates coord : gameRules.getNeighbors(playerEntity.getCoordinates())) {
					if(!coords.contains(coord)) {
						if(gameRules.getEntityAt(coord) == null) {
							coords.add(coord);
						}
					}
				}
			}
		}
		return coords;
	}

	private static List<EntityCoordinates> GetUnitAttacks(GameRules gameRules, PlayerState player, GameEntity unit) {
		GameState gameState = gameRules.getGameState();
		List<EntityCoordinates> targetCoords = new ArrayList<>();
		PlayerState opponent = gameState.getOpponent(player);
		for(GameEntity candidate : gameState.getInPlay(opponent)) { 
			if(CanAttack(gameRules, unit, candidate)) {
				targetCoords.add(candidate.getCoordinates());
			}
		}
		return targetCoords;
	}
	
	private static List<EntityCoordinates> GetUnitMoves(GameRules gameRules, PlayerState player, GameEntity unit) {
		List<EntityCoordinates> targetCoords = new ArrayList<>();
		for(EntityCoordinates coord : gameRules.getNeighbors(unit.getCoordinates())) {
			if(gameRules.getEntityAt(coord) == null) {
				targetCoords.add(coord);
			}
		}
		return targetCoords;
	}
	
	private static boolean HasResources(PlayerState player, GameEntity card) {
		int energyCost = card.getStat(EntityStat.ENERGY_COST);
		if(player.getResource(EntityStat.ENERGY_RESOURCE) < energyCost) return false;
		int mineralCost = card.getStat(EntityStat.MINERAL_COST);
		if(player.getResource(EntityStat.MINERAL_RESOURCE) < mineralCost) return false;
		return true;
	}
	
	private static boolean CanAttack(GameRules game, GameEntity attacker, GameEntity candidate) {
		int distance = EntityCoordinates.distance(attacker.getCoordinates(), candidate.getCoordinates());
		if(distance > attacker.getRange()) return false;
		return true;
	}
	
	// Where can various cards summon their unit to?
	private Map<Integer, List<EntityCoordinates>> validSummons = new HashMap<Integer, List<EntityCoordinates>>();
	// Where can various cards be played?
	private Map<Integer, List<EntityCoordinates>> validPowers = new HashMap<Integer, List<EntityCoordinates>>();
	// What cards can be played without a target?
	private List<Integer> validNoTarget = new ArrayList<>();
	// Where can various units move to?
	private Map<Integer, List<EntityCoordinates>> validMoves = new HashMap<>();
	// Where can various unit attack?
	private Map<Integer, List<EntityCoordinates>> validAttacks = new HashMap<>();
	// Where can cards be sacrificed/discarded?
	private Map<Integer, List<EntityCoordinates>> validDiscards = new HashMap<>();

	private PlayValidator() {}
	
	public Map<Integer, List<EntityCoordinates>> getValidSummons() {
		return validSummons;
	}
	
	public Map<Integer, List<EntityCoordinates>> getValidAttacks() {
		return validAttacks;
	}
	
	public Map<Integer, List<EntityCoordinates>> getValidMoves() {
		return validMoves;
	}
	
	public Map<Integer, List<EntityCoordinates>> getValidPowerPlays() {
		return validPowers;
	}
	
	public List<Integer> getValidNoTarget() {
		return validNoTarget;
	}
	
	/**
	 * Theoretically, we don't expect this to fail unless there's a bug or the client 
	 * has been messed with.
	 * 
	 * @param entity
	 * @param coord
	 * @return
	 */
	public boolean isValidSummon(GameEntity entity, int column, int row) {
		// If it's a unit and in hand:
		if(entity != null && entity.isUnit() && entity.inHand()) {
			if(validSummons.containsKey(entity.getId())) {
				for(EntityCoordinates c : validSummons.get(entity.getId())) {
					if(c.x == column && c.y == row) {
						return true;
					}
				}
			} 
		}
		logger.warn("isValidSummon unexpectedly returned false");
		return false;
	}
	
	public boolean isValidPower(GameEntity entity, int column, int row) {
		if(validPowers.containsKey(entity.getId())) {
			for(EntityCoordinates c : validPowers.get(entity.getId())) {
				if(c.x == column && c.y == row) {
					return true;
				}
			}
		}
		if(validNoTarget.contains(entity.getId())) {
			return true;
		}
		logger.warn("isValidPower unexpectedly returned false");
		return false;
	}

	public boolean isValidAttack(GameEntity attacker, int column, int row) {
		if(validAttacks.containsKey(attacker.getId())) {
			for(EntityCoordinates c : validAttacks.get(attacker.getId())) {
				if(c.x == column && c.y == row) {
					return true;
				}
			}
		}
		logger.warn("isValidAtack unexpectedly returned false");
		return false;
	}
	
	public boolean isValidMove(GameEntity entity, int x, int y) {
		if(validMoves.containsKey(entity.getId())) {
			for(EntityCoordinates c : validMoves.get(entity.getId())) {
				if(c.x == x && c.y == y) {
					return true;
				}
			}
		}
		logger.warn("isValidMove unexpectedly returned false");
		return false;
	}
	
	public boolean isValidDiscard(GameEntity entity, int x, int y) {
		if(validDiscards.containsKey(entity.getId())) {
			for(EntityCoordinates c : validDiscards.get(entity.getId())) {
				if(c.x == x && c.y == y) {
					return true;
				}
			}
		}
		logger.warn("isValidDiscard unexpectedly returned false");
		return false;
	}

}
