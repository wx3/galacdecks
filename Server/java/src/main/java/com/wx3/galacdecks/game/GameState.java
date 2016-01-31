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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wx3.galacdecks.gameevents.GameEvent;

/**
 * The GameState defines the complete current state
 * of a game in play. GameState does not perform any game 
 * logic, that is handled by {@link GameInstance}, but it
 * does manage its own internal data structures, such as maintaining
 * lists of in play entities and board locations.  
 * 
 * It is copyable so that an AI can perform simulations with it.
 * 
 * @author Kevin
 *
 */
public class GameState {
	
	final static Logger logger = LoggerFactory.getLogger(GameState.class);
	int turn = 1;
	
	private PlayerState[] players;
	private boolean isStarted = false;
	
	// The idCounter is used for assigning entity ids, unique to each
	// game instance:
	private int idCounter = 1;
	
	private Map<Integer, GameEntity> entityLookup = new HashMap<>();
	
	// Map of valid coordinates to entities. Null represents a valid, but empty location
	private Map<EntityCoordinates, GameEntity> entityMap;
	
	private List<GameEntity> inPlay = new ArrayList<GameEntity>();
	private boolean gameOver = false;
	private PlayerState winner = null;
	
	// Events that have not been processed are stored in the event queue:
	Queue<GameEvent> eventQueue = new ConcurrentLinkedQueue<GameEvent>();
	
	/**
	 * Verify that two gamestates are equivalent, but not containing references to the same 
	 * objects. This is important for testing so we know the AI, which evaluates copies of the 
	 * game, is not modifying the original.
	 * @param state1
	 * @param state2
	 * @return
	 */
	public static boolean separateButEqual(GameState state1, GameState state2) {
		if(state1 == state2) return false;
		if(state1.isStarted != state2.isStarted) return false;
		if(state1.idCounter != state2.idCounter) return false;
		if(state1.gameOver != state2.gameOver) return false;
		if(state1.players.length != state2.players.length) return false;
		if(state1.entityMap.size() != state2.entityMap.size()) return false;
		if(!PlayerState.separateButEqual(state1.players[0], state2.players[0])) return false;
		if(!PlayerState.separateButEqual(state1.players[1], state2.players[1])) return false;
		return true;
	}
	
	public GameState(List<EntityCoordinates> coords) {
		this.entityMap = new HashMap<>();
		players = new PlayerState[2];
		players[0] = new PlayerState(1);
		players[1] = new PlayerState(2);
		for(EntityCoordinates coord : coords) {
			entityMap.put(coord, null);
		}
	}
	
	public GameState(int columns, int rows) {
		this.entityMap = new HashMap<>();
		players = new PlayerState[2];
		players[0] = new PlayerState(1);
		players[1] = new PlayerState(2);
		for(int y = 0; y < rows; y++) {
			for(int x = 0; x < columns; x++) {
				EntityCoordinates coord = new EntityCoordinates(x, y);
				entityMap.put(coord,  null);	
			}
		}
	}

	/**
	 * A copy of a GameState should reproduce the same
	 * behaviors from the same commands as the original,
	 * without modifying the original's state.
	 * @param original
	 */
	public GameState(GameState original) {
		isStarted = original.isStarted;
		turn = original.turn;
		idCounter = original.idCounter;
		players = new PlayerState[original.players.length];
		for(PlayerState op : original.players) {
			if(op != null) {
				PlayerState np = new PlayerState(op);
				for(GameEntity ne : np.deck) {
					registerEntity(ne);
				}
				for(GameEntity ne : np.hand) {
					registerEntity(ne);
				}
				players[op.position - 1] = np;
			}
		}
		entityMap = new HashMap<>();
		for(Map.Entry<EntityCoordinates, GameEntity> entry : original.entityMap.entrySet()) {
			entityMap.put(entry.getKey(), null);
		}
		for(GameEntity oe : original.inPlay) {
			PlayerState newOwner = null;
			if(oe.getOwner() != null) {
				newOwner = getPlayer(oe.getOwner().getPosition());
			}
			GameEntity ce = new GameEntity(oe, newOwner);
			inPlay.add(ce);
			registerEntity(ce);
			if(ce.onBoard()) {
				entityMap.put(ce.getCoordinates(), ce);
			}
		}
	}
	
	public void start() {
		this.isStarted = true;
	}
	
	public boolean isStarted() {
		return isStarted;
	}
	
	private void registerEntity(GameEntity entity) {
		entityLookup.put(entity.getId(), entity);
	}
	
	GameEntity createEntity(EntityPrototype prototype) {
		GameEntity entity;
		if(prototype == null) {
			entity = new GameEntity(idCounter);
		}
		else {
			entity = new GameEntity(idCounter, prototype);
		}
		++idCounter;
		entityLookup.put(entity.getId(), entity);
		return entity;
	}
	
	public int getTurn() {
		return turn;
	}
	
	/**
	 * Get the player for the provided position. The first player
	 * is in index 0, the second player is in index 1.
	 * @param pos
	 * @return
	 */
	public PlayerState getPlayer(int pos) {
		if(pos > players.length) {
			throw new IndexOutOfBoundsException("Player position out of bounds.");
		}
		if(pos < 1) {
			throw new IndexOutOfBoundsException("Player position must be > 0.");
		}
		return players[pos - 1];
	}
	
	public PlayerState getCurrentPlayer() {
		int pos = ((turn + 1) % 2) + 1;
		return getPlayer(pos);
	}
	
	public PlayerState getOpponent(PlayerState player) {
		int pos = 1;
		if(player.position == 1) pos = 2;
		return getPlayer(pos);
	}
	
	List<PlayerState> getPlayers() {
		return Arrays.asList(players);
	}
	
	void putInPlay(GameEntity entity) {
		inPlay.add(entity);
		entity.setTag(EntityTag.IN_PLAY);
	}
	
	void placeEntity(GameEntity entity, EntityCoordinates coord) {
		if(!entityMap.containsKey(coord)) {
			throw new RuntimeException("Invalid entity position: " + coord);
		}
		if(entityMap.get(coord) != null) {
			throw new RuntimeException("Position occupied: " + coord);
		}
		
		if(!entity.isUnit()) {
			throw new RuntimeException("Entity must be unit");
		}
		// If the entity is already on the board, remove it from its previous board location:
		if(entity.onBoard()) {
			if(entityMap.get(entity.getCoordinates()) == null) {
				throw new RuntimeException("Entity in play not found at location on board.");
			}
			entityMap.put(entity.getCoordinates(), null);
		}
		// If the entity isn't in play, play it
		if(!entity.inPlay()) {
			putInPlay(entity);
		}
		// Finally set its position:
		entity.setPosition(coord);
		entityMap.put(entity.getCoordinates(), entity);
	}
	
	void removeEntity(GameEntity entity) {
		if(entity.inPlay()) {
			if(!inPlay.remove(entity)) {
				throw new RuntimeException("Entity marked as in play but not found inPlay list");
			}
			if(entity.onBoard()) {
				if(entityMap.get(entity.getCoordinates()) == null) {
					throw new RuntimeException("Entity in play not found at location on board.");
				}
				entityMap.put(entity.getCoordinates(), null);
			}
		} 
		if(entityLookup.remove(entity.getId()) != entity) {
			throw new RuntimeException("Entity in lookup did not match entity removed");
		}
		entity.clearTag(EntityTag.IN_PLAY);
		entity.setTag(EntityTag.REMOVED);
	}
	
	public List<GameEntity> getInPlay() {
		return new ArrayList<GameEntity>(inPlay);
	}
	
	public List<GameEntity> getInPlay(PlayerState player) {
		return inPlay.stream().filter(x -> x.getOwner() == player).collect(Collectors.toList());
	}
	
	public List<GameEntity> getInPlayWithTag(EntityTag tag) {
		return inPlay.stream().filter(x -> x.hasTag(tag)).collect(Collectors.toList());
	}
	
	public Collection<GameEntity> getAllEntities() {
		return Collections.unmodifiableCollection(entityLookup.values());
	}
	
	public boolean isOnBoard(EntityCoordinates coord) {
		return entityMap.containsKey(coord);
	}
	
	public Collection<EntityCoordinates> getAllCoordinates() {
		return entityMap.keySet();
	}
	
	public GameEntity getEntity(int id) {
		if(entityLookup.containsKey(id)) return entityLookup.get(id);
		return null;
	}
	
	public GameEntity getEntityAt(int x, int y) {
		return getEntityAt(new EntityCoordinates(x, y));
	}
	
	public GameEntity getEntityAt(EntityCoordinates coord) {
		return entityMap.get(coord);
	}
	
	void gameOver(PlayerState winner) {
		gameOver = true;
		this.winner = winner;
	}
	
	public boolean isGameOver() {
		return gameOver;
	}
	
	public PlayerState getWinner() {
		return winner;
	}

}
