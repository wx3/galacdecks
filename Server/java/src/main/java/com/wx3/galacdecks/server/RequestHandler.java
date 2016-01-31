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

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.wx3.galacdecks.game.AttackCommand;
import com.wx3.galacdecks.game.DiscardCommand;
import com.wx3.galacdecks.game.EndTurnCommand;
import com.wx3.galacdecks.game.GameCommand;
import com.wx3.galacdecks.game.GameInstance;
import com.wx3.galacdecks.game.GameOverHandler;
import com.wx3.galacdecks.game.GameSystem;
import com.wx3.galacdecks.game.MoveCommand;
import com.wx3.galacdecks.game.PlayPowerCommand;
import com.wx3.galacdecks.game.PlayerInstance;
import com.wx3.galacdecks.game.SummonUnitCommand;
import com.wx3.galacdecks.game.messages.JoinMessage;
import com.wx3.galacdecks.server.requests.JoinRequest;
import com.wx3.galacdecks.server.requests.NewGameRequest;
import com.wx3.galacdecks.server.servermessages.GameCreatedMessage;
import com.wx3.galacdecks.server.servermessages.GuestCreatedMessage;

/**
 * Translates JSON requests into the appropriate ClientRequest and 
 * passes them to the appropriate hander.
 * 
 * @author Kevin
 *
 */
public class RequestHandler implements GameOverHandler {
	
	private static int counter = 0;
	
	private GameServer gameServer;
	private MessageHandler messageHandler;
	private User user;
	private PlayerInstance player;
	private Gson gson;
	
	public RequestHandler(GameServer gameServer, MessageHandler messageHandler) {
		this.gameServer = gameServer;
		this.messageHandler = messageHandler;
		gson = new Gson();
	}
	
	public void handleJsonRequest(JsonObject json) throws BadRequestException {
		if(!json.has("requestClass")) {
			throw new BadRequestException("requestClass missing.");
		}
		String requestClass = json.get("requestClass").getAsString();
		if(user == null) {
			switch(requestClass) {
				case "CreateGuestRequest":
					this.user = handleCreateGuest(json); 
		    		break;
		    	default:
		    		throw new BadRequestException("Can't handle request class '" + requestClass + "'");
			}
		} else {
			if(requestClass.equals("ConcedeRequest")) {
				handleConcede(json);
				return;
			}
			if (player == null) {
				switch(requestClass) {
					case "NewGameRequest":
						handleNewGameRequest(json);
						break;
					case "JoinRequest":
						handleJoinRequest(json);
						break;
					
					default:
						throw new BadRequestException("Can't handle request class '" + requestClass + "'");
				}
			}
			else {
				GameCommand command;
				switch(requestClass) {
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
					case "MoveCommand" :
						command = gson.fromJson(json, MoveCommand.class);
						break;
					case "DiscardCommand" : 
						command = gson.fromJson(json, DiscardCommand.class);
						break;
					default:
						throw new BadRequestException("Can't handle request class '" + requestClass + "'");
				}
				player.handleCommand(command);
			}
		}
	}
	
	public void gameOver() {
		player = null;
	}
	
	public void disconnect() {
		player.disconnect();
	}
	
	private User handleCreateGuest(JsonObject json) {
		int ackId = json.get("ackId").getAsInt();
		SecureRandom random = new SecureRandom();
		String password = new BigInteger(130, random).toString(32);
		User user = User.CreateUser("guest", password);
		gameServer.createUser(user);
		GuestCreatedMessage message = new GuestCreatedMessage(ackId, user, password);
		messageHandler.handleMessage(message);
		return user;
	}
	
	private void handleNewGameRequest(JsonObject json) throws BadRequestException {
		NewGameRequest request = gson.fromJson(json, NewGameRequest.class);
		GameInstance game = gameServer.newOrOpenGameInstance(request.getSystemId());
		GameSystem system = game.getSystem();
		Map<Integer, Long> reservations = game.getReservations();
		if(system.pvp) {
			int preferredPos = (counter % 2) + 1;
			int secondPos;
			if(preferredPos == 1) secondPos = 2;
			else secondPos = 1;
			if(!reservations.containsKey(preferredPos)) {
				game.reservePosition(preferredPos, user.getUserId());
			} else if(!reservations.containsKey(secondPos)) {
				game.reservePosition(secondPos, user.getUserId());
			} else {
				throw new BadRequestException("Game unexpectedly had no open positions");
			}
		}
		else {
			game.reservePosition(1, user.getUserId());
		}
		
		GameCreatedMessage message = new GameCreatedMessage(request.getAckId(), game, request.getSystemId());
		messageHandler.handleMessage(message);
		++counter;
	}
	
	private void handleJoinRequest(JsonObject json) throws BadRequestException {
		JoinRequest request = gson.fromJson(json, JoinRequest.class);
		GameInstance game = gameServer.getGame(request.getGameId());
		if(game == null) {
			throw new BadRequestException("Unable to find game with id " + request.getGameId() + " on server");
		}
		
		int position = game.getReservation(user.getUserId());
		if(position < 0) {
			throw new BadRequestException("Player had no reservation in game");
		}
		player = new PlayerInstance(game.getGameId(), user.getUsername(), user.getUserId(), position);
		player.connect(messageHandler, game);
		player.setGameOverHandler(this);
		game.join(player);
		JoinMessage message = new JoinMessage(request.getAckId(), game, position);
		messageHandler.handleMessage(message);
		// Try to start the game:
		game.start();
	}
	
	private void handleConcede(JsonObject json) throws BadRequestException {
		if(player != null) {
			
		}
	}

}
