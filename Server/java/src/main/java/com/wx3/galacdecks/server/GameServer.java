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
package com.wx3.galacdecks.server;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.wx3.galacdecks.ai.AiManager;
import com.wx3.galacdecks.ai.EvaluatorAI;
import com.wx3.galacdecks.ai.GameAI;
import com.wx3.galacdecks.datastore.GameDatastore;
import com.wx3.galacdecks.game.AttackCommand;
import com.wx3.galacdecks.game.EndTurnCommand;
import com.wx3.galacdecks.game.GameCommand;
import com.wx3.galacdecks.game.GameInstance;
import com.wx3.galacdecks.game.GameSystem;
import com.wx3.galacdecks.game.PlayPowerCommand;
import com.wx3.galacdecks.game.PlayerInstance;
import com.wx3.galacdecks.game.SummonUnitCommand;

/**
 * @author Kevin
 *
 */
public class GameServer {
	
	// Games older than this are removed (finished or not):
	public static final int GAME_EXPIRATION_SECONDS = 3600;
	// PvP games older than this have an AI attached if no one else has joined:
	public static final int AUTO_AI_SECONDS = 3;
	
	final static Logger logger = LoggerFactory.getLogger(GameServer.class);
	
	private GameDatastore datastore;
	private AiManager aiManager;
	private GameServerUpdateTask updateTask;
	private Map<Long, GameInstance> gameInstances = new HashMap<Long, GameInstance>();
	
	public GameServer(GameDatastore datastore) {
		this.datastore = datastore;
	}
	
	public GameDatastore getDataStore() {
		return datastore;
	}
	
	GameInstance getGameInstance(long id) {
		if(gameInstances.containsKey(id)) {
			return gameInstances.get(id);
		}
		return null;
	}
	
	public void start(float aiUpdate) {
		aiManager = new AiManager(aiUpdate);
		aiManager.start();
		
		updateTask = new GameServerUpdateTask(this);
		Timer timer = new Timer();
		timer.schedule(updateTask, 0, 1000);
	}

	public void createUser(User user) {
		datastore.createUser(user);
	}
	
	public int activeGameCount() {
		return gameInstances.size();
	}
	
	public GameInstance getGame(long gameId) {
		if(gameInstances.containsKey(gameId)) {
			return gameInstances.get(gameId);
		}
		return null;
	}
	
	synchronized void update() {
		Iterator<Map.Entry<Long, GameInstance>> iter = gameInstances.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<Long, GameInstance> entry = iter.next();
			GameInstance game = entry.getValue();
			long now = (new Date()).getTime();
			long age = now - game.getCreated().getTime();
			// Look for expired games:
			if(age > GAME_EXPIRATION_SECONDS * 1000) {
				logger.info("Removing " + game + " (expired timestamp)");
				game.terminate();
				iter.remove();
			}
			if(game.isOpen() && age > AUTO_AI_SECONDS * 1000) {
				if(game.getPlayer(1) == null) {
					attachAi(game, 1);
				} else {
					attachAi(game, 2);
				}
				logger.info("Auto-adding AI player");
			}
		}
	}
	
	public synchronized  GameInstance newGameInstance(String gameSystemId) {
		GameSystem gameSystem = datastore.getSystem(gameSystemId);
		if(gameSystem == null) {
			throw new RuntimeException("Unable to find game system with id '" + gameSystemId + "'");
		}
		
		//List<String> deck = Bootstrap.GetDefaultDeck();
		
		GameInstance game = new GameInstance(gameSystem);
		datastore.newGame(game);
		gameInstances.put(game.getGameId(), game);
		return game;
	}
	
	public synchronized GameInstance newOrOpenGameInstance(String gameSystemId) {
		GameInstance game = null;
		GameSystem gameSystem = datastore.getSystem(gameSystemId);
		if(gameSystem == null) {
			throw new RuntimeException("Unable to find game system with id '" + gameSystemId + "'");
		}
		// If the game system is PvP, check open games first:
		if(gameSystem.pvp) {
			// Try to find an open game with the supplied gameSystemId:
			for(GameInstance candidate : gameInstances.values()) {
				if(candidate.getSystem().id.equals(gameSystemId)) {
					if(candidate.isOpen()) {
						game = candidate;
					}
				}
			}
		} 
		if(game == null) {
			game = newGameInstance(gameSystemId);
		}
		if(!gameSystem.pvp) {
			attachAi(game, 2);
		}
		return game;
	}
	
	public synchronized void attachAi(GameInstance game, int position) {
		PlayerInstance p1 = new PlayerInstance(game.getGameId(), "AI", 0, position);
		GameAI ai1 = new EvaluatorAI(p1);
		p1.connect(ai1, game);
		game.join(p1);
		aiManager.registerAI(ai1);
		game.start();
	}
	
	public GameCommand createCommand(PlayerInstance player, JsonObject json) {
		if(json == null) {
			throw new RuntimeException("Suppied Json cannot be null");
		}
		GameCommand command = null;
		if(!json.has("commandClass")) {
			throw new RuntimeException("Json missing command type");
		}
		String commandName = json.get("commandClass").getAsString();
		Gson gson = new Gson();
		// We explicitly create the command objects from strings here because
		// we only allow clients to create commands we define:
		switch(commandName) {
			case "EndTurnCommand" : 
				command = gson.fromJson(json, EndTurnCommand.class);
				break;
			case "SummonUnitCommand" :
				command = gson.fromJson(json, SummonUnitCommand.class);
				break;
			case "PlayPowerCommand" :
				command = gson.fromJson(json, PlayPowerCommand.class);
				break;
			case "AttackCommand" :
				command = gson.fromJson(json, AttackCommand.class);
				break;
			default : 
				throw new RuntimeException("Unknown command '" + commandName + "'");
		}
		command.setPlayer(player);
		logger.info("Received " + command);
		return command;
	}
	
	/**
	 * Inner class for running scheduled updates
	 * @author Kevin
	 *
	 */
	class GameServerUpdateTask extends TimerTask {
		
		private GameServer gameServer;
		
		public GameServerUpdateTask(GameServer gameServer) {
			this.gameServer = gameServer;
		}

		@Override
		public void run() {
			gameServer.update();
		}
	
	}
}

