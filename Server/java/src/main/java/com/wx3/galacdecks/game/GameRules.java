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
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wx3.galacdecks.Bootstrap;
import com.wx3.galacdecks.game.EntityTag;
import com.wx3.galacdecks.gameevents.ClientPrefabEvent;
import com.wx3.galacdecks.gameevents.ConsumeResourceEvent;
import com.wx3.galacdecks.gameevents.DamageCausedEvent;
import com.wx3.galacdecks.gameevents.DamageTakenEvent;
import com.wx3.galacdecks.gameevents.DeathEvent;
import com.wx3.galacdecks.gameevents.DiscardEvent;
import com.wx3.galacdecks.gameevents.DrawCardEvent;
import com.wx3.galacdecks.gameevents.EndTurnEvent;
import com.wx3.galacdecks.gameevents.GameEvent;
import com.wx3.galacdecks.gameevents.GameOverEvent;
import com.wx3.galacdecks.gameevents.MoveEvent;
import com.wx3.galacdecks.gameevents.PlayCardEvent;
import com.wx3.galacdecks.gameevents.ReturnCardEvent;
import com.wx3.galacdecks.gameevents.ShieldRechargeEvent;
import com.wx3.galacdecks.gameevents.StartGameEvent;
import com.wx3.galacdecks.gameevents.SummonUnitEvent;
import com.wx3.galacdecks.gameevents.UpdateResourceEvent;

/**
 * The GameRules is the logic of the game. It modifies the game state
 * based on actions and the resulting {@link GameEvent}. No persistent
 * state is stored in a GameRules instance-- the state of any game
 * should be defined entirely by its gamestate.
 * 
 * 
 * All GameEvent logic uses the public methods of the GameRules to 
 * effect game play.
 * 
 * @author Kevin
 *
 */
public class GameRules {

	private static ScriptEngine scriptEngine;
	private static final int MAX_EVENTS = 1000;
	
	// Name of the phase that buff rules listen for:
	public static final String BUFF_PHASE = "BuffPhase";
	public static final String DEFAULT_SHIP = "EXPLORER";
	public static final int MAX_CARDS = 8;
	public static final int MAX_RESOURCES = 10;

	private GameSystem gameSystem;
	private Map<String, EntityPrototype> cards;
	private Map<String, EntityRule> entityRules;
	private GameState gameState;	
	private GameOverHandler gameOverHandler;
	
	private List<GameEvent> processedEvents = new ArrayList<>();
	
	final static Logger logger = LoggerFactory.getLogger(GameRules.class);
	
	private static ScriptEngine getScriptEngine() {
		if(scriptEngine == null) {
			NashornScriptEngineFactory factory = new NashornScriptEngineFactory(); 
			scriptEngine = factory.getScriptEngine(new RestrictiveFilter());
			if(scriptEngine == null) {
				throw new RuntimeException("Unable to get script engine");
			}
		}
		return scriptEngine;
	}
	
	/**
	 * Don't allow scripts to access general Java classes.
	 *  
	 * @author Kevin
	 *
	 */
	static class RestrictiveFilter implements ClassFilter {
		@Override
		public boolean exposeToScripts(String s) {
			return false;
		}
	}	

	public GameRules(GameSystem gameSystem) {
		this.gameSystem = gameSystem;
		this.cards = gameSystem.getPrototypes();
		if(cards == null || cards.size() == 0) {
			throw new RuntimeException("GameSystem did not define any prototypes");
		}
		this.entityRules = gameSystem.getRules();
		List<EntityCoordinates> coords = new ArrayList<EntityCoordinates>();
		for(int y = 0; y < 5; y++) {
			for(int x = 0; x < 9; x++) {
				coords.add(new EntityCoordinates(x, y));
			}
		}
		coords.remove(new EntityCoordinates(0, 4));
		coords.remove(new EntityCoordinates(1, 4));
		coords.remove(new EntityCoordinates(3, 4));
		coords.remove(new EntityCoordinates(5, 4));
		coords.remove(new EntityCoordinates(7, 4));
		coords.remove(new EntityCoordinates(8, 4));		
		coords.remove(new EntityCoordinates(0, 0));
		coords.remove(new EntityCoordinates(8, 0));

		gameState = new GameState(coords);
	}
	
	public GameRules(GameRules original) {
		this.cards = original.cards;
		this.entityRules = original.entityRules;
		this.gameState = new GameState(original.getGameState());
	} 
	
	public void setGameOverHandler(GameOverHandler handler) {
		this.gameOverHandler = handler;
	}

	/**
	 * Get a ScriptContext with the GameRules bound to it so its methods are available.
	 *
	 * @return
	 * @throws NoSuchMethodException
	 * @throws ScriptException
	 */
	ScriptContext getScriptContext() throws NoSuchMethodException, ScriptException {
		ScriptEngine script = getScriptEngine();
		ScriptContext scriptContext = new SimpleScriptContext();
		scriptContext.setBindings(script.createBindings(), ScriptContext.ENGINE_SCOPE);
		bindGameToScript(scriptContext);
		return scriptContext;
	}
	
	/**
	 * Binds to the javascript global object so that GameInstance methods
	 * can be called without needing to preface every one with an object reference,
	 * e.g.: "endTurn()" instead of "game.endTurn()".
	 * 
	 * @throws ScriptException
	 * @throws NoSuchMethodException
	 */
	private void bindGameToScript(ScriptContext scriptContext) throws ScriptException, NoSuchMethodException {
		ScriptEngine engine = getScriptEngine();
		Object global = engine.eval("this", scriptContext);
		Object jsObject = engine.eval("Object", scriptContext);
		((Invocable) engine).invokeMethod(jsObject, "bindProperties", global, this);
	}
	
	/**
	 * Create the root entity with the appropriate rules and put it in play.
	 */
	void start() {
		GameEntity rootEntity = spawnEntity();
		rootEntity.setName("ROOT");
		rootEntity.setTag(EntityTag.ROOT);
		for(String rootRule : gameSystem.rootRules) {
			addRule(rootEntity, rootRule);	
		}
		gameState.putInPlay(rootEntity);
		addEvent(new StartGameEvent());
		gameState.start();
		processEvents();
		startTurn();
		processEvents();
		processedEvents.clear();
		logger.info("Game started for system " + gameSystem);
	}
	
	synchronized PlayValidator getValidPlaysForPlayer(PlayerState playerState) {
		PlayValidator validPlays = PlayValidator.GenerateValidPlays(this, playerState);
		return validPlays;
	}
	
	public synchronized List<GameEvent> handleCommand(GameCommand command) throws BadCommandException {
		if(!isStarted()) {
			throw new BadCommandException("Game not started");
		}
		PlayerState playerState = gameState.getPlayer(command.getPlayerPosition());
		if(gameState.getCurrentPlayer() != playerState) {
			throw new BadCommandException("Not player's turn");
		}
		command.execute(this);
		processEvents();
		List<GameEvent> events = new ArrayList<>(processedEvents);
		processedEvents.clear();
		return events;
	}
	
	public GameSystem getSystem() {
		return gameSystem;
	}	
	
	public boolean isStarted() {
		return gameState.isStarted();
	}
	
	void setPlayer(int pos, PlayerInstance player) {
		PlayerShip playerShip = new PlayerShip();

		playerShip.setStartingDeck(Bootstrap.GetDefaultDeck());
		player.setShip(playerShip);
		initPlayer(player);
	}
	
	/**
	 * Send a string to the logger. This is useful for testing rule scripts.
	 * @param message
	 */
	public void debug(String message) {
		logger.info(message);
	}
	
	private void calculateMaxResource() {
		PlayerState currentPlayer = gameState.getCurrentPlayer();
		int energy = 0;
		for(GameEntity entity : gameState.getInPlay(currentPlayer)) {
			energy += entity.getEnergyResource();
		}
		if(energy > MAX_RESOURCES) energy = MAX_RESOURCES;
		currentPlayer.setMaxResource(EntityStat.ENERGY_RESOURCE, energy);
	}
	
	private void recalculateMinerals(PlayerState player) {
		int mineralResource = 0;
		int mineralCost = 0;
		for(GameEntity entity : gameState.getInPlay(player)) {
			mineralResource += entity.getMineralResource();
			mineralCost += entity.getMineralCost();
		}
		if(mineralResource > MAX_RESOURCES) mineralResource = MAX_RESOURCES;
		player.setMaxResource(EntityStat.MINERAL_RESOURCE, mineralResource);
		int mineralRemaining = mineralResource - mineralCost;
		if(mineralRemaining < 0) mineralRemaining = 0;
		player.setResource(EntityStat.MINERAL_RESOURCE, mineralRemaining);
	}
	
	void startTurn() {
		logger.info(this + " starting turn " + gameState.getTurn());
		PlayerState currentPlayer = gameState.getCurrentPlayer();
		calculateMaxResource();
		for(GameEntity entity : gameState.getInPlay()) {
			entity.turnReset();
		}
		currentPlayer.setResource(EntityStat.ENERGY_RESOURCE, 0);
		addEnergy(currentPlayer, currentPlayer.getMaxResource(EntityStat.ENERGY_RESOURCE));
		
		for(GameEntity entity : gameState.getInPlay()) {
			int amount = entity.getMaxShields() - entity.getCurrentShields();
			rechargeShields(entity, amount);
		}
	}

	
	/// Methods executed by commands:
	
	void endTurn() {
		// Recalc max resources for the player who just ended their turn:
		calculateMaxResource();
		for(GameEntity entity : gameState.getInPlay()) {
			entity.deductDisable();
		}
		++gameState.turn;
		addEvent(new EndTurnEvent(gameState));
		logger.info("Turn is now " + gameState.turn);
	}
	
	/**
	 * Play a unit from a card in hand
	 * @param entity
	 * @param column
	 * @param row
	 */
	void playUnitFromHand(GameEntity card, int column, int row) {
		card.clearTag(EntityTag.IN_HAND);
		if(!card.getOwner().hand.remove(card)) {
			throw new RuntimeException("Tried to summon a unit that wasn't in hand.");
		}
		EntityCoordinates coord = new EntityCoordinates(column, row);
		consumeEnergy(card, card.getStat(EntityStat.ENERGY_COST));
		consumeMinerals(card, card.getStat(EntityStat.MINERAL_COST));
		addEvent(new PlayCardEvent(card, column, row));
		
		gameState.placeEntity(card, coord);
		EntityPrototype prototype = cards.get(card.getPrototypeId());
		addEvent(new SummonUnitEvent(card, prototype));
	}
	
	void playPowerFromHand(GameEntity card, int x, int y) {
		card.clearTag(EntityTag.IN_HAND);
		if(!card.getOwner().hand.remove(card)) {
			throw new RuntimeException("Tried to play a card that wasn't in hand.");
		}
		consumeEnergy(card, card.getStat(EntityStat.ENERGY_COST));
		consumeMinerals(card, card.getStat(EntityStat.MINERAL_COST));
		GameEntity target = null;
		if(x >= 0) {
			target = gameState.getEntityAt(x, y);
		}
		gameState.putInPlay(card);
		addEvent(new PlayCardEvent(card, x, y, target));
	}
	
	void discardCard(GameEntity card, int x, int y) {
		card.clearTag(EntityTag.IN_HAND);
		if(!card.getOwner().hand.remove(card)) {
			throw new RuntimeException("Tried to discard a card that wasn't in hand.");
		}
		GameEntity target = null;
		if(x >= 0) {
			target = gameState.getEntityAt(x, y);
		}
		if(target == null) {
			throw new RuntimeException("Discard requires a target.");
		}
		target.useDiscard();
		addEvent(new DiscardEvent(card, target));
	}

	
	/// End methods executed by commands 
	
	public Map<String, EntityPrototype> getCards() {
		return Collections.unmodifiableMap(cards);
	}

	
	public EntityPrototype getEntityPrototype(GameEntity entity) {
		if(cards.containsKey(entity.getPrototypeId())) {
			return cards.get(entity.getPrototypeId());
		}
		return null;
	}
	
	public PlayerState getPlayerState(int playerPos) {
		return gameState.getPlayer(playerPos);
	}
	
	/**
	 * Draw a card for a player, removing from their deck and adding it to their
	 * hand. If triggerEvent is TRUE, produce the draw card event (the default, 
	 * except for at the start of game).
	 * 
	 * @param player
	 * @param triggerEvent Should this produce an event?
	 * @return The card drawn
	 */
	public GameEntity playerDrawCard(PlayerState player, boolean triggerEvent) {
		if(player.hand.size() >= MAX_CARDS) {
			return null;
		}
		if(player.deck.size() < 1) {
			return null;
		}
		GameEntity draw = player.deck.remove(0);
		draw.clearTag(EntityTag.IN_DECK);
		draw.setTag(EntityTag.IN_HAND);
		player.hand.add(draw);
		if(triggerEvent) {
			addEvent(new DrawCardEvent(player, draw));
			//processEvents();
		}
		return draw;
	}
	
	public GameEntity playerDrawCard(PlayerState player) {
		return playerDrawCard(player, true);
	}
	
	/**
	 * Put a card at the top of a player's deck.
	 * @param player
	 * @param cardId
	 * @return
	 */
	public GameEntity addDeck(PlayerState player, String cardId) {
		GameEntity entity = createCard(cardId);
		entity.setOwner(player);
		player.deck.add(0, entity);
		return entity;
	}
	
	public void shuffleDeck(PlayerState player) {
		Collections.shuffle(player.deck);
	}
	
	/**
	 * Remove the cards from the player's hand, and draw one for each card 
	 * removed.
	 * 
	 * @param player
	 * @param cards
	 */
	public void mulligan(PlayerState player, List<GameEntity> cards) {
		// Iterate over a copy, to avoid concurrent mod if we're passed the hand:
		List<GameEntity> cardList = new ArrayList<>(cards);
		int count = cardList.size();
		// First, remove the cards from the player's hand:
		for(GameEntity card : cardList) {
			if(player.hand.contains(card)) {
				card.clearTag(EntityTag.IN_HAND);
				player.hand.remove(card);
				addEvent(new ReturnCardEvent(card));
			} else {
				throw new RuntimeException("Cannot mulligan card not in hand");
			}
		}
		// Then shuffle the deck and draw the appropriate number of cards:
		Collections.shuffle(player.deck);
		for(int i = 0; i < count; i++) {
			playerDrawCard(player);
		}
		// Finally return the cards to the deck:
		for(GameEntity card : cardList) {
			card.setTag(EntityTag.IN_DECK);	
			player.deck.add(card);
		}
		Collections.shuffle(player.deck);
	}
	
	public void addRule(GameEntity entity, String ruleId) {
		if(!entityRules.containsKey(ruleId)) {
			throw new RuntimeException("Unknown ruleId '" + ruleId + "'");
		}
		EntityRule rule = entityRules.get(ruleId);
		entity.addRule(rule);
		recalculateStats();
	}
	
	/**
	 * Summon a specific unit by id for a player at a location.
	 * @param player
	 * @param cardId
	 * @param x
	 * @param y
	 * @return
	 */
	public GameEntity summonUnit(PlayerState player, String cardId, int x, int y) {
		EntityCoordinates coord = new EntityCoordinates(x, y);
		GameEntity unit = summonUnit(cardId, coord);
		unit.setOwner(player);
		return unit;
	}
	
	public GameEntity summonUnit(PlayerState player, String cardId, EntityCoordinates coord) {
		GameEntity unit = summonUnit(cardId, coord);
		unit.setOwner(player);
		return unit;
	}
	
	
	public GameEntity summonUnit(String cardId, int x, int y) {
		return summonUnit(cardId, new EntityCoordinates(x, y));
	}
	
	/**
	 * Summon an uncontrolled unit at the supplied location
	 * @param cardId
	 * @param x
	 * @param y
	 * @return
	 */
	public GameEntity summonUnit(String cardId, EntityCoordinates coord) {
		GameEntity unit = createCard(cardId);
		unit.clearTag(EntityTag.IN_DECK);
		gameState.placeEntity(unit, coord);
		EntityPrototype prototype = cards.get(unit.getPrototypeId());
		addEvent(new SummonUnitEvent(unit, prototype));
		return unit;
	}
	
	public void moveUnit(GameEntity entity, EntityCoordinates coord) {
		gameState.placeEntity(entity, coord);
		addEvent(new MoveEvent(entity, coord));
	}
	
	public void dealDamage(GameEntity cause, GameEntity target, int amount) {
		String damagePrefab = "Default Attack";
		EntityPrototype prototype = cards.get(cause.getPrototypeId());
		if(!prototype.getProjectile().isEmpty()) {
			damagePrefab = prototype.getProjectile();
		}
		dealDamage(cause, target, amount, damagePrefab);
	}
	
	public void dealDamage(GameEntity cause, GameEntity target, int amount, String prefabName) {
		int blocked = target.getCurrentShields();
		if(blocked > amount) blocked = amount;
		target.dischargeShields(blocked);
		int damageDealt = amount - blocked;
		target.damage(damageDealt);
		DamageEffect damage = new DamageEffect(target.getId(), damageDealt, blocked);
		addEvent(new DamageCausedEvent(cause, damage, prefabName));
		if(damage.damageTaken > 0) {
			addEvent(new DamageTakenEvent(target, damage));
		}
		if(target.getCurrentHealth() <= 0) {
			killEntity(target);
		}
	}
	
	
	public void repair(GameEntity target, int amount) {
		target.repair(amount);
	}
	
	/**
	 * Attempts to recharge the entity's shields by amount. If the shields 
	 * were recharged by more than 0, adds an event.
	 * 
	 * @param entity
	 * @param amount
	 */
	public void rechargeShields(GameEntity entity, int amount) {
		amount = entity.rechargeShields(amount);
		if(amount > 0) {
			addEvent(new ShieldRechargeEvent(entity, amount));	
		}
	}
	
	public void killEntity(GameEntity entity) {
		removeEntity(entity);
		addEvent(new DeathEvent(entity));
	}
	
	public void clientEvent(String prefab, GameEntity entity) {
		addEvent(new ClientPrefabEvent(prefab, entity));
	}
	
	public void clientEvent(String prefab) {
		addEvent(new ClientPrefabEvent(prefab, null));
	}
	
	GameEntity spawnEntity() {
		return spawnEntity(null);
	}
	
	GameEntity spawnEntity(EntityPrototype prototype) {
		GameEntity entity = gameState.createEntity(prototype);
		return entity;
	}
	
	public GameEntity getEntity(int id) {
		return gameState.getEntity(id);
	}
	
	void removeEntity(GameEntity entity) {
		entity.setTag(EntityTag.REMOVED);
	}
	
	public GameState getGameState() {
		return gameState;
	}
	
	public PlayerState getOpponent() {
		return gameState.getOpponent(gameState.getCurrentPlayer());
	}
	
	public PlayerState getOpponent(PlayerState player) {
		return gameState.getOpponent(player);
	}
	
	public GameEntity getHomeworld(PlayerState player) {
		return gameState.getEntity(player.homeworldId);
	}	
	
	public int getEnergy(PlayerState player) {
		return player.getResource(EntityStat.ENERGY_RESOURCE);
	}
	
	public int getMinerals(PlayerState player) {
		return player.getResource(EntityStat.MINERAL_RESOURCE);
	}
	
	/**
	 * Returns a stream of all entities in play (on board, plus invisible entities such as root 
	 * and powers that haven't been cleaned up).
	 * @return
	 */
	public Stream<GameEntity> inPlay() {
		return gameState.getInPlay().stream();
	}
	
	/**
	 * Returns a stream of all units on the board.
	 * @return
	 */
	public Stream<GameEntity> onBoard() {
		return gameState.getAllEntities().stream().filter(x -> x.onBoard() && !x.isRemoved());
	}
	
	/**
	 * Returns the entity at the supplied coordinates or null if empty:
	 * 
	 * @param coord
	 * @return
	 */
	public GameEntity getEntityAt(EntityCoordinates coord) {
		return gameState.getEntityAt(coord);
	}
	
	public GameEntity getEntityAt(int x, int y) {
		EntityCoordinates coord = new EntityCoordinates(x, y);
		return getEntityAt(coord);
	}
	
	/**
	 * Return true if the supplied coordinates are on the board.
	 * 
	 * @param coord
	 * @return
	 */
	public boolean isOnBoard(EntityCoordinates coord) {
		return gameState.isOnBoard(coord);
	}
	
	/**
	 * Returns a stream of all units on the board owned by owner.
	 * 
	 * @param owner
	 * @return
	 */
	public Stream<GameEntity> onBoard(PlayerState owner) {
		return gameState.getAllEntities().stream().filter(x -> x.onBoard() && x.getOwner() == owner);
	}
	
	/**
	 * Return a list of all entities on the board matching the predicate
	 * 
	 * @param predicate
	 * @return
	 */
	public List<GameEntity> onBoard(Predicate<GameEntity> predicate) {
		List<GameEntity> entities = onBoard().collect(Collectors.toList());
		Collections.shuffle(entities);
		return entities.stream().filter(predicate).collect(Collectors.toList());
	}
	
	/**
	 * Returns a stream of all units on the board that are owned by the enemy of owner.
	 * @param owner
	 * @return
	 */
	public Stream<GameEntity> enemyUnits(PlayerState owner) {
		return onBoard(gameState.getOpponent(owner));
	}
	
	/**
	 * Return a random unit on the board that matches the supplied predicate, or null.
	 * 
	 * @param predicate
	 * @return
	 */
	public GameEntity randomUnit(Predicate<GameEntity> predicate) {
		List<GameEntity> entities = onBoard().collect(Collectors.toList());
		Collections.shuffle(entities);
		return entities.stream().filter(predicate).findFirst().orElse(null);
	}
	
	public EntityCoordinates randomCoordinates(Predicate<EntityCoordinates> predicate) {
		List<EntityCoordinates> coordinates = new ArrayList<EntityCoordinates>(gameState.getAllCoordinates());
		Collections.shuffle(coordinates);
		return coordinates.stream().filter(predicate).findFirst().orElse(null);
	}
	
	
	/**
	 * Get a list of coordinates that border the supplied coordinates.
	 * @param coord
	 * @return
	 */
	public List<EntityCoordinates> getNeighbors(EntityCoordinates coord, boolean includeSelf) {
		List<EntityCoordinates> neighbors = new ArrayList<>();
		if(includeSelf) {
			neighbors.add(coord);
		}
		for(HexDirection dir : HexDirection.values()) {
			EntityCoordinates candidate = coord.add(dir);
			if(isOnBoard(candidate)) {
				neighbors.add(candidate);
			}
		}
		return neighbors;
	}
	
	public List<EntityCoordinates> getNeighbors(EntityCoordinates coord) {
		return getNeighbors(coord, false);
	}
	
	public List<GameEntity> getNeighborEntities(EntityCoordinates coord) {
		List<GameEntity> entities = new ArrayList<>();
		for(HexDirection dir : HexDirection.values()) {
			EntityCoordinates candidate = coord.add(dir);
			GameEntity e = getEntityAt(candidate);
			if(e != null) entities.add(e);
		}
		return entities;
	}
	
	/**
	 * Are the supplied coordinates neighbors?
	 * @param coord1
	 * @param coord2
	 * @return
	 */
	public boolean areNeighbors(EntityCoordinates coord1, EntityCoordinates coord2) {
		for(HexDirection dir : HexDirection.values()) {
			if(coord1.add(dir).equals(coord2)) return true;
		}
		return false;
	}
	
	public boolean areNeighbors(GameEntity entity1, GameEntity entity2) {
		if(entity1 ==  null || entity2 == null) return false;
		return areNeighbors(entity1.getCoordinates(), entity2.getCoordinates());
	}
	
	public void addEnergy(PlayerState player, int amount) {
		addResource(player, EntityStat.ENERGY_RESOURCE, amount);
		addEvent(new UpdateResourceEvent(player, EntityStat.ENERGY_RESOURCE, player.getResource(EntityStat.ENERGY_RESOURCE)));
	}
	
	public void addMinerals(PlayerState player, int amount) {
		addResource(player, EntityStat.MINERAL_RESOURCE, amount);
		addEvent(new UpdateResourceEvent(player, EntityStat.MINERAL_RESOURCE, player.getResource(EntityStat.MINERAL_RESOURCE)));
	}
	
	public void consumeEnergy(GameEntity consumer, int amount) {
		PlayerState player = consumer.getOwner();
		consumeResource(player, EntityStat.ENERGY_RESOURCE, amount);
		addEvent(new ConsumeResourceEvent(consumer, consumer.getOwner(), EntityStat.ENERGY_RESOURCE, amount));
	}
	
	public void consumeMinerals(GameEntity consumer, int amount) {
		PlayerState player = consumer.getOwner();
		consumeResource(player, EntityStat.MINERAL_RESOURCE, amount);
		addEvent(new ConsumeResourceEvent(consumer, consumer.getOwner(), EntityStat.MINERAL_RESOURCE, amount));
	}
	
	private void addResource(PlayerState player, EntityStat resource, int amount) {
		if(amount < 0) {
			throw new IllegalArgumentException("Amount must be positve");
		}
		player.setResource(resource, player.getResource(resource) + amount);
	}
	
	/**
	 * Deduct amount from the player's resource. 
	 * 
	 * @param player
	 * @param resourceName
	 * @param amount
	 */
	private void consumeResource(PlayerState player, EntityStat resource, int amount) {
		int current = player.getResource(resource);
		current -= amount;
		if(current < 0) {
			throw new RuntimeException(resource + " consumption is greater than available.");
		}
		player.setResource(resource, current);
	}
	
	public void addEvent(GameEvent event) {
		gameState.eventQueue.add(event);
	}
	
	private void createPlayerDeck(PlayerState player, List<String> startingDeck) {
		player.deck = new ArrayList<GameEntity>();
		for(String id : startingDeck) {
			if(!cards.containsKey(id)) {
				throw new RuntimeException("Could not find card named '" + id + "' in card lookup.");
			}
			GameEntity entity = createCard(id);
			entity.setOwner(player);
			player.deck.add(entity);
		}
		Collections.shuffle(player.deck);
	}
	
	private void initPlayer(PlayerInstance player) {
		PlayerShip ship = player.getShip();
		if(ship == null) {
			throw new RuntimeException("Cannot init player without ship");
		}
		PlayerState playerState = gameState.getPlayer(player.getPosition());
		if(!playerState.isDeckInitialized() && gameSystem.usePlayerDecks) {
			createPlayerDeck(playerState, ship.getStartingDeck());
		}
		playerState.name = player.getPlayerName();
		playerState.shipPrefab = ship.getShipPrefab();
		
	}
	
	private GameEntity createCard(String cardId) {
		EntityPrototype prototype = cards.get(cardId);
		if(prototype == null) {
			throw new RuntimeException("Unable to find prototype '" + cardId + "'");
		}
		GameEntity entity = gameState.createEntity(prototype);
		entity.setTag(EntityTag.IN_DECK);
		return entity;
	}
	
	/**
	 * The player concedes the game and loses
	 * @param player
	 * @return
	 */
	public void concede(PlayerState player) {
		GameEntity homeworld = gameState.getEntity(player.homeworldId);
		removeEntity(homeworld);
		gameOverCheck();
	}
	
	/**
	 * Check whether the game is over (one of the player's homeworlds removed), if so
	 * mark the game as over and return true. Otherwise, return false;
	 * @return
	 */
	boolean gameOverCheck() {
		if(gameState.isGameOver()) return true;
		for(PlayerState playerState : gameState.getPlayers()) {
			GameEntity homeworld = gameState.getEntity(playerState.homeworldId);
			if(homeworld == null || homeworld.isRemoved()) {
				PlayerState opponent = gameState.getOpponent(playerState);
				GameEntity opponentHomeworld = gameState.getEntity(opponent.homeworldId);
				if(opponentHomeworld == null || opponentHomeworld.isRemoved()) {
					gameState.gameOver(null);
					return true;
				} else {
					gameState.gameOver(opponent);
					return true;
				}
			}
		}
		return false;
	}
	
	void processEvents() {
		int i = 0;
		while(!gameState.eventQueue.isEmpty()) {
			GameEvent event = gameState.eventQueue.poll();
			// Iterate over a copy of entities to avoid ConcurrentModification exceptions
			// if a rule spawns an entity:
			List<GameEntity> entityList = new ArrayList<GameEntity>(gameState.getInPlay());
			for(GameEntity entity : entityList) {
				for(EntityRule rule : entity.getRules()) {
					if(!entity.isRemoved() && !entity.isDisabled()) {
						processRule(event, rule, entity);	
					}
				}	
			}
			recalculateStats();
			recalculateMinerals(gameState.getCurrentPlayer());
			recalculateFickle();
			processedEvents.add(event);
			++i;
			if(i > MAX_EVENTS) {
				throw new RuntimeException("Exceeded max events: " + MAX_EVENTS);
			}
		}
		// After all events are processed, remove any entities marked for removal and powers.
		List<GameEntity> entityList = new ArrayList<GameEntity>(gameState.getInPlay());
		for(GameEntity entity : entityList) {
			if(entity.isPower() || entity.isRemoved()) {
				gameState.removeEntity(entity);
			}
		}
		if(gameOverCheck()) {
			logger.info("Game over");
			processedEvents.add(new GameOverEvent(gameState.getWinner()));
			if(gameOverHandler != null) {
				gameOverHandler.gameOver();
			}
		}
	}
	
	/**
	 * Change ownership of fickle units
	 */
	private void recalculateFickle() {
		List<GameEntity> fickleEntities = gameState.getInPlayWithTag(EntityTag.FICKLE);
		PlayerState p1 = getPlayerState(1);
		PlayerState p2 = getPlayerState(2);
		for(GameEntity fickle : fickleEntities) {
			int influence = 0;
			for(GameEntity e : getNeighborEntities(fickle.getCoordinates())) {
				if(!e.isRemoved()) {
					if(e.getOwner().position == 1) {
						influence += e.getAttack();
					}
					if(e.getOwner().position == 2) {
						influence -= e.getAttack();
					}
				}
			}
			if(influence > 0) {
				fickle.setOwner(p1);
			}
			else if(influence < 0) {
				fickle.setOwner(p2);
			} else {
				fickle.setOwner(null);
			}
		}
	}
	
	
	/**
	 * Evaluate the {@link EntityRule} attached to an entity, in the context of a 
	 * triggering event.
	 * 
	 * @param event
	 * @param rule
	 * @param entity
	 */
	private void processRule(GameEvent event, EntityRule rule, GameEntity entity) {
		try {
			if(rule.isTriggered(event)) {
				ScriptContext scriptContext = getScriptContext();
				Bindings scriptScope = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
				// Let the rule access the event, game and entity objects:
				scriptScope.put("event", event);
				scriptScope.put("card", null);
				scriptScope.put("entity", entity);
				scriptScope.put("coord", entity.getCoordinates());
				scriptScope.put("owner", entity.getOwner());
				getScriptEngine().eval(rule.getScript(),scriptContext);
			}
		} catch (final ScriptException se) {
			throw new RuntimeException("Error in rule: " + rule.getId(), se.getCause());
		} catch (Exception ex) {
			throw new RuntimeException("Unexpected error in processing rule: " + rule.getId() + ", " + ex.getMessage(), ex.getCause());
		}
	}

	
	boolean validatePlayCard(GameEntity card, EntityCoordinates coord, ValidatorScript validator) {
		try {
			ScriptContext scriptContext = getScriptContext();
			Bindings scriptScope = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
			GameEntity target = getEntityAt(coord);
			scriptScope.put("event", null);
			scriptScope.put("card", card);
			scriptScope.put("owner", card.getOwner());
			scriptScope.put("coord", coord);
			scriptScope.put("target", target);
			scriptScope.put("error", null);
			scriptScope.put("valid", false);
			getScriptEngine().eval(validator.getScript(), scriptContext);
			boolean valid = (boolean) scriptScope.get("valid");
			return valid;
		} catch (final ScriptException se) {
			logger.error("Scripting exception: " + se.getMessage());
			return false;
		} catch (Exception ex) {
			logger.error("Non-scripting exception: " + ex.getMessage());
			return false;
		}
	}
	
	boolean validateDiscard(GameEntity card, GameEntity target, ValidatorScript validator) {
		try {
			ScriptContext scriptContext = getScriptContext();
			Bindings scriptScope = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
			scriptScope.put("event", null);
			scriptScope.put("card", card);
			scriptScope.put("owner", target.getOwner());
			scriptScope.put("target", target);
			scriptScope.put("error", null);
			scriptScope.put("valid", false);
			getScriptEngine().eval(validator.getScript(), scriptContext);
			boolean valid = (boolean) scriptScope.get("valid");
			return valid;
		} catch (final ScriptException se) {
			logger.error("Scripting exception: " + se.getMessage());
			return false;
		} catch (Exception ex) {
			logger.error("Non-scripting exception: " + ex.getMessage());
			return false;
		}
	}

	/**
	 * Resets all entities' stats to their base values, then evaluates
	 * all buff rules to update them.
	 */
	void recalculateStats() {
		int beforeSize = gameState.eventQueue.size();
		// First, reset all stats. This sets each stat to the base value:
		for(GameEntity entity : gameState.getAllEntities()) {
			entity.resetStats();
		}
		// Then iterate over all entities in play and trigger any buff rules. These rules
		// may modify one or more entity's stats:
		for(GameEntity entity : gameState.getInPlay()) {
			for(EntityRule rule : entity.getRules()) {
				if(rule.getEventTrigger().equals(BUFF_PHASE)) {
					try {
						ScriptContext scriptContext = getScriptContext();
						Bindings scriptScope = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
						scriptScope.put("event", null);
						scriptScope.put("rules", this);
						scriptScope.put("entity", entity);
						scriptScope.put("coord", entity.getCoordinates());
						scriptScope.put("owner", entity.getOwner());
						getScriptEngine().eval(rule.getScript(), scriptContext);	
					} catch (Exception ex) {
						throw new RuntimeException("Exception processing buff " + rule + ":" + ex.getMessage());
					}
					if(gameState.eventQueue.size() != beforeSize) {
						throw new RuntimeException(rule + " modified event queue, not allowed during buff phase.");
					}
				}
			}
		}
	}
	
}
