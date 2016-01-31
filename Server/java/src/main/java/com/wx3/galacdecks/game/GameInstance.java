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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wx3.galacdecks.game.messages.EventsMessage;
import com.wx3.galacdecks.game.messages.GameInitMessage;
import com.wx3.galacdecks.game.messages.JoinMessage;
import com.wx3.galacdecks.gameevents.EventView;
import com.wx3.galacdecks.gameevents.GameEvent;

/**
 * A GameInstance is the association between the players and a game.
 * 
 * @author Kevin
 *
 */
@Entity
@Table(name="game_instances")
public class GameInstance implements GameOverHandler {
	
	final static Logger logger = LoggerFactory.getLogger(GameInstance.class);
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)	
	private long gameId;
	private Date created;
	
	private transient Map<Integer, PlayerInstance> players = new HashMap<Integer, PlayerInstance>();
	private transient GameRules gameRules;
	
	// Positions reserved for particular players (key is position, value is player id):
	private transient Map<Integer, Long> reservations = new HashMap<Integer, Long>();
	
	public GameInstance(GameSystem gameSystem) {
		if(gameSystem == null) {
			throw new IllegalArgumentException("Game system cannot be null;");
		}
		this.created = new Date();
		gameRules = new GameRules(gameSystem);
		gameRules.setGameOverHandler(this);
	}
	
	public void join(PlayerInstance player) {
		if(players.containsKey(player.getPosition())) {
			throw new RuntimeException("Can't join, another player already in  " + player.getPosition());
		}
		if(reservations.containsKey(player.getPosition())) {
			long resPlayerId = reservations.get(player.getPosition());
			if(resPlayerId != player.getUserId()) {
				throw new RuntimeException("Position is reserved for another player: " + resPlayerId);
			}
		}
		players.put(player.getPosition(), player);
		gameRules.setPlayer(player.getPosition(), player);
	}
	
	public void reservePosition(int position, long userId) {
		if(reservations.containsKey(position)) {
			throw new RuntimeException("Position " + position + " already reserved");
		}
		reservations.put(position, userId);
	}
	
	/**
	 * Returns the position reserved for the supplied player, or -1 if no
	 * reservation is found.
	 * 
	 * @param userId
	 * @return
	 */
	public int getReservation(long userId) {
		if(reservations.containsKey(1)) {
			if(reservations.get(1) == userId) {
				return 1;
			}
		}
		if(reservations.containsKey(2)) {
			if(reservations.get(2) == userId) {
				return 2;
			}
		}
		return -1;
	}
	
	/**
	 * Is this GameInstance open for players to join?
	 * @return
	 */
	public boolean isOpen() {
		if(players.containsKey(1) && players.containsKey(2)) {
			return false;
		}
		if(reservations.containsKey(1) && reservations.containsKey(2)) {
			return false;
		}
		return true;
	}
	
	public Map<Integer, Long> getReservations() {
		return Collections.unmodifiableMap(reservations);
	}
	
	public synchronized boolean start() {
		if(players.get(1) == null || players.get(2) == null) {
			return false;
		}
		gameRules.start();
		for(PlayerInstance player : players.values()) {
			sendGameInitMessage(player);
			player.sendValidPlays();
		}
		logger.info("Game started.");
		return true;
	}
	
	public long getGameId() {
		return gameId;
	}
	
	public GameSystem getSystem() {
		return gameRules.getSystem();
	}
	
	public Date getCreated() {
		return created;
	}
	
	public void gameOver() {
		for(PlayerInstance player : players.values()) {
			player.gameOver();
		}
	}
	
	public void terminate() {
		gameRules.getGameState().gameOver(null);
	}
	
	public PlayerInstance getPlayer(int position) {
		if(players.containsKey(position)) {
			return players.get(position);
		}
		return null;
	}
	
	public PlayerState getPlayerState(PlayerInstance playerInstance) {
		return gameRules.getGameState().getPlayer(playerInstance.getPosition());
	}
	
	public GameView getViewForPlayer(PlayerInstance playerInstance) {
		PlayerState viewer = getPlayerState(playerInstance);
		GameView view = new GameView(gameRules.getGameState(), viewer);
		return view;
	}

	public GameRules getGameRules() {
		return gameRules;
	}	
	
	public void sendGameInitMessage(PlayerInstance playerInstance) {
		GameInitMessage message = new GameInitMessage(getViewForPlayer(playerInstance), gameRules.getCards());
		playerInstance.handleMessage(message);
	}
	
	/**
	 * Execute a command and send the results to players.
	 * 
	 * @param command
	 * @return
	 * @throws BadCommandException 
	 */
	public synchronized void handleCommand(GameCommand command) throws BadCommandException {
		GameState gameState = gameRules.getGameState();
		if(gameState.isGameOver()) {
			logger.warn("Can't handle command, game is over");
			return;
		}
		List<GameEvent> events;
		events = gameRules.handleCommand(command);

		// Send each player a message of the events. This is customized per
		// player because players may have different views of the same event:
		for(PlayerInstance player : players.values()) {
			if(player.isConnected()) {
				PlayerState playerState = getPlayerState(player);
				List<EventView> eventViews = new ArrayList<EventView>();
				for(GameEvent event : events) {
					eventViews.add(event.getPlayerView(playerState));
				}
				GameView view = new GameView(gameRules.getGameState(), playerState);
				EventsMessage message = new EventsMessage(command, eventViews, view);
				player.handleMessage(message);
			}
		}
		
		if(!gameState.isGameOver()) {
			for(PlayerInstance player : players.values()) {
				if(player.isConnected()) {
					if(gameState.getCurrentPlayer() == player.getPlayerState()) {
						player.sendValidPlays();
					}	
				}
			}
		}
	}
	
	public PlayValidator getValidPlays(PlayerInstance player) {
		PlayValidator validPlays = gameRules.getValidPlaysForPlayer(player.getPlayerState());
		return validPlays;
	}
	
}
