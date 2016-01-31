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
package com.wx3.galacdecks.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import com.wx3.galacdecks.game.AttackCommand;
import com.wx3.galacdecks.game.BadCommandException;
import com.wx3.galacdecks.game.EndTurnCommand;
import com.wx3.galacdecks.game.EntityCoordinates;
import com.wx3.galacdecks.game.EntityPrototype;
import com.wx3.galacdecks.game.EntityStat;
import com.wx3.galacdecks.game.GameCommand;
import com.wx3.galacdecks.game.GameEntity;
import com.wx3.galacdecks.game.GameRules;
import com.wx3.galacdecks.game.GameState;
import com.wx3.galacdecks.game.MoveCommand;
import com.wx3.galacdecks.game.PlayPowerCommand;
import com.wx3.galacdecks.game.PlayValidator;
import com.wx3.galacdecks.game.PlayerInstance;
import com.wx3.galacdecks.game.PlayerState;
import com.wx3.galacdecks.game.SummonUnitCommand;

/**
 * An EvaluatorAI simulates all legal actions for the player, then
 * evaluates the gamestate based on various criteria and chooses the
 * action that produces the highest value.
 * 
 * @author Kevin
 *
 */
public class EvaluatorAI extends GameAI {
	
	private static ScriptEngine scriptEngine;
	
	private ScriptContext scriptContext;
	
	// Map of coordinates along a tactical value:
	private Map<EntityCoordinates, Float> tacticalHints = new HashMap<EntityCoordinates, Float>();
	
	private static ScriptEngine getScriptEngine() {
		if(scriptEngine == null) {
			NashornScriptEngineFactory factory = new NashornScriptEngineFactory(); 
			scriptEngine = factory.getScriptEngine();
			if(scriptEngine == null) {
				throw new RuntimeException("Unable to get script engine");
			}
		}
		return scriptEngine;
	}
	
	public EvaluatorAI(PlayerInstance player) {
		this.player = player;
	}
	
	/**
	 * Get a ScriptContext with the GameRules bound to it so its methods are available.
	 *
	 * @return
	 * @throws NoSuchMethodException
	 * @throws ScriptException
	 */
	private ScriptContext getScriptContext(GameRules gameRules) {
		
		ScriptEngine script = getScriptEngine();
		scriptContext = new SimpleScriptContext();
		scriptContext.setBindings(script.createBindings(), ScriptContext.ENGINE_SCOPE);
		try {
			Object global = script.eval("this", scriptContext);
			Object jsObject = script.eval("Object", scriptContext);
			((Invocable) script).invokeMethod(jsObject, "bindProperties", global, gameRules);
		} catch (Exception e) {
			throw new RuntimeException("Exception obtaining script context: " + e.getMessage());
		}
		return scriptContext;
	}
	
	/**
	 * Get a collection of all valid commands available to our player.
	 * @return
	 */
	private Collection<GameCommand> getCommandChoices() {
		List<GameCommand> commandChoices = new ArrayList<GameCommand>();
		PlayValidator playValidator = player.getValidPlays();
		Map<Integer, List<EntityCoordinates>> validSummons = playValidator.getValidSummons();
		for(int entityId : validSummons.keySet()) {
			for(EntityCoordinates coord : validSummons.get(entityId)) {
				SummonUnitCommand command = new SummonUnitCommand(entityId, coord.x, coord.y);
				commandChoices.add(command);
			}
		}
		Map<Integer, List<EntityCoordinates>> validPowerPlays = playValidator.getValidPowerPlays();
		for(int entityId : validPowerPlays.keySet()) {
			for(EntityCoordinates coord : validPowerPlays.get(entityId)) {
				PlayPowerCommand command = new PlayPowerCommand(entityId, coord.x, coord.y);
				commandChoices.add(command);
			}
		}
		List<Integer> validNoTarget = playValidator.getValidNoTarget();
		for(int cardId : validNoTarget) {
			PlayPowerCommand command = new PlayPowerCommand(cardId, -1, -1);
			commandChoices.add(command);
		}
		Map<Integer, List<EntityCoordinates>> validAttacks = playValidator.getValidAttacks();
		for(int attackerId : validAttacks.keySet()) {
			for(EntityCoordinates coord : validAttacks.get(attackerId)) {
				AttackCommand command = new AttackCommand(attackerId, coord.x, coord.y);
				commandChoices.add(command);
			}
		}
		Map<Integer, List<EntityCoordinates>> validMoves = playValidator.getValidMoves();
		for(int entityId : validMoves.keySet()) {
			for(EntityCoordinates coord :validMoves.get(entityId)) {
				MoveCommand command = new MoveCommand(entityId, coord);
				commandChoices.add(command);
			}
		}
		return commandChoices;
	}
	
	@Override
	public GameCommand getBestCommand() {
		GameRules originalRules = player.getGameInstance().getGameRules();
		// Used to verify the simulations don't corrupt the original game:
		GameRules testCopy = new GameRules(originalRules); 
		GameCommand bestCommand = null;
		if(tacticalHints.size() == 0) calculateTacticalMap(originalRules);
		// The starting bestVal is the value of the current state of the game:
		float bestVal = evaluateGameState(originalRules);
		logger.info("Current gamestate val: " + bestVal);
		// Iterate over all available command choices, simulate the command and evaluate the resulting game state.
		// Choose the best one.
		Collection<GameCommand> commandChoices = getCommandChoices();
		int i = 0;
		for(GameCommand command : commandChoices) {
			GameRules result;
			try {
				result = simulateCommand(command, originalRules);
				++i;
			} catch (BadCommandException e) {
				throw new RuntimeException("AI picked bad command, " + command +  ": " + e.getMessage());
			}
			float val = evaluateGameState(result);
			if(val >= bestVal) {
				bestCommand = command;
				bestVal = val;
			}
		} 
		logger.info("Ran " + i + " simulations");
		if(bestCommand == null) bestCommand = chooseEndTurn(originalRules);
		
		assert(GameState.separateButEqual(testCopy.getGameState(), originalRules.getGameState()));
		return bestCommand;
	}
	
	private void calculateTacticalMap(GameRules game) {
		// First, find the (approximate) center coord of the board:
		int maxX = 0;
		int maxY = 0;
		for(EntityCoordinates coord : game.getGameState().getAllCoordinates()) {
			if(coord.x > maxX) maxX = coord.x;
			if(coord.y > maxY) maxY = coord.y;
		}
		EntityCoordinates center = new EntityCoordinates(maxX / 2, maxY / 2);
		logger.info("Center is " + center);
	}
	
	private EndTurnCommand chooseEndTurn(GameRules game) {
		
		float energyValue = 0;
		float mineralValue = 0;
		float cardValue = 0;
		GameState gameState = game.getGameState();
		PlayerState myPlayer = gameState.getPlayer(player.getPosition());
		
		for(GameEntity card : myPlayer.hand) {
			energyValue += card.getStat(EntityStat.ENERGY_COST);
			mineralValue += card.getStat(EntityStat.MINERAL_COST);
		}
		energyValue -= myPlayer.getResource(EntityStat.ENERGY_RESOURCE);
		mineralValue -= myPlayer.getResource(EntityStat.MINERAL_RESOURCE);
		if(energyValue < 1) energyValue = 1;
		if(myPlayer.getResource(EntityStat.ENERGY_RESOURCE) >= 10) energyValue = 0;
		if(mineralValue < 1) mineralValue = 1;
		if(myPlayer.getResource(EntityStat.MINERAL_RESOURCE) >= 10) mineralValue = 0;
		cardValue += (GameRules.MAX_CARDS - myPlayer.getHandSize()) * 2;
		if(cardValue < 1) cardValue = 1;
		
		double total = energyValue + mineralValue + cardValue;
		double rand = Math.random() * total;
		if(rand < energyValue) {
			return new EndTurnCommand(true, false, false);
		} else if(rand < energyValue + mineralValue) {
			return new EndTurnCommand(false, true, false);
		} else {
			return new EndTurnCommand(false, false, true);
		}
		
	}
	
	private GameRules simulateCommand(GameCommand command, GameRules gameRules) throws BadCommandException {
		GameRules gameCopy = new GameRules(gameRules);	
		command.setPlayer(player);
		gameCopy.handleCommand(command);
		return gameCopy;
	}
	
	private float evaluateGameState(GameRules gameRules) {
		float val = 0;
		GameState gameState = gameRules.getGameState();
		PlayerState myPlayer = gameState.getPlayer(player.getPosition());
		for(GameEntity entity : gameState.getInPlay()) {
			EntityPrototype prototype = gameRules.getEntityPrototype(entity);
			float entityVal = 0;
			if(entity.isUnit() && entity.onBoard()) {
				entityVal = defaultUnitEvaluator(gameRules, entity, myPlayer);
				if(prototype != null) {
					for(AiHint hint : prototype.getAiHints()) {
						float customVal = evaluateCustomRule(gameRules, hint.getScript(), entity, myPlayer);
						entityVal += customVal;
					}	

				}
			} else {
				
			}
			val += entityVal;
		}
		val += evaluateHand(myPlayer.hand);
		return val;
	}
	
	private float defaultUnitEvaluator(GameRules game, GameEntity entity, PlayerState me) {
		float val = baseTacticalValue(entity);
		if(entity.getOwner() != me) {
			val = -val;
		}
		return val;
	}
	
	/**
	 * The base tactical value of an entity is the sum of its AI_TACTICAL stat and damage per turn
	 * modified by current health.
	 * 
	 * @param entity
	 * @return
	 */
	private float baseTacticalValue(GameEntity entity) {
		float val = 1;
		float tactical = entity.getStat(EntityStat.AI_TACTICAL);
		float damage = entity.getAttack() * (1 + entity.getStat(EntityStat.EXTRA_ACTIONS));
		val += tactical + damage;
		// Plus a boost for current health
		val += ((float) entity.getCurrentHealth()) * 0.75f;
		// If an entity is damaged, it's worth 50% to 100% of its tactical value depending on the damage
		// percent:
		if(entity.getMaxHealth() > 0) {
			float healthPercent = (float) (entity.getCurrentHealth()) / (float) (entity.getMaxHealth());
			val = (val / 2) + (val * healthPercent / 2);
		}
		return val;
	}

	private float evaluateCustomRule(GameRules gameRules, String rule, GameEntity entity, PlayerState me) {
		try {
			float val = 0;
			Bindings scriptScope = getScriptContext(gameRules).getBindings(ScriptContext.ENGINE_SCOPE);
			scriptScope.put("me", me);
			scriptScope.put("owner", entity.getOwner());
			scriptScope.put("entity", entity);
			scriptScope.put("boardValue", 0);
			scriptScope.put("handValue", 0);
			scriptScope.put("coordinates", entity.getCoordinates());
			getScriptEngine().eval(rule, scriptContext);
			if(entity.onBoard()) {
				val = Float.parseFloat(scriptScope.get("boardValue").toString());
			}
			else if(entity.inHand()) {
				val = Float.parseFloat(scriptScope.get("handValue").toString());
			}
			return val;
		} catch (final ScriptException se) {
			logger.error("Scripting exception: " + se.getMessage());
			return 0;
		} catch (Exception ex) {
			logger.error("Non-scripting exception: " + ex.getMessage());
			return 0;
		}
	}

	private float evaluateHand(List<GameEntity> hand) {
		float val = hand.size();
		for(GameEntity card : hand) {
			float tactical = card.getStat(EntityStat.AI_TACTICAL);
			if(card.isUnit()) {
				val += tactical * 0.25f;
			}
			if(card.isPower()) {
				val += tactical;
			}
		}
		return val;
	}

}
