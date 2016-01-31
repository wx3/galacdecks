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

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wx3.galacdecks.game.messages.ValidPlaysMessage;
import com.wx3.galacdecks.server.MessageHandler;
import com.wx3.galacdecks.server.OutboundMessage;


/**
 * A player's connection to a particular game instance.
 * 
 * @author Kevin
 *
 */
@Entity
@Table(name="player_instances", 
	uniqueConstraints = {@UniqueConstraint(columnNames={"authtoken"})},
	indexes = { @Index(columnList = "authtoken")}
)
public class PlayerInstance implements GameOverHandler {
	
	final static Logger logger = LoggerFactory.getLogger(PlayerInstance.class);
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	private String authtoken;
	private long gameId;
	private long userId;
	private String playerName;
	private byte position = -1;
	
	@Transient
	private PlayerShip playerShip;
	
	@Transient
	private GameInstance game;
	
	@Transient
	private boolean connected = false;
	
	@Transient
	private MessageHandler messageHandler;
	
	@Transient
	private GameOverHandler gameOverHandler;
		
	public static String generateToken() {
		SecureRandom random = new SecureRandom();
		return  new BigInteger(130, random).toString(32);
	}
	
	public PlayerInstance() {
		
	}

	
	public PlayerInstance(long gameId, String playerName, long userId, int position) {
		this.gameId = gameId;
		this.playerName = playerName + "_" + userId;
		this.userId = userId;
		this.authtoken = generateToken();
		setPosition(position);
	}
	
	public long getId() {
		return id;
	}

	public String getAuthToken() {
		return authtoken;
	}
	
	public String getPlayerName() {
		return playerName;
	}
	
	public long getUserId() {
		return userId;
	}
	
	public long getGameId() {
		return gameId;
	}
	
	public int getPosition() {
		return position;
	}
	
	public PlayerState getPlayerState() {
		return game.getPlayerState(this);
	}
	
	public void setPosition(int position) {
		if(position < 1 || position > 2) {
			throw new RuntimeException("Position must be 1 or 2");
		}
		this.position = (byte) position;
	}
	
	public PlayerShip getShip() {
		return playerShip;
	}
	
	public void setShip(PlayerShip ship) {
		this.playerShip = ship;
	}
	
	public GameInstance getGameInstance() {
		return this.game;
	}
	
	public void setGameInstance(GameInstance game) {
		this.game = game;
	}
	
	public void connect(MessageHandler messageHandler, GameInstance game) {
		this.messageHandler = messageHandler;
		this.game = game;
		this.connected = true;
	}
	
	public void setGameOverHandler(GameOverHandler gameOverHandler) {
		this.gameOverHandler = gameOverHandler;
	}
	
	public void gameOver() {
		if(this.gameOverHandler != null) {
			this.gameOverHandler.gameOver();
		}
	}
	
	public PlayValidator getValidPlays() {
		return game.getValidPlays(this);
	}
	
	public void sendValidPlays() {
		PlayValidator validPlays = getValidPlays();
		ValidPlaysMessage validsMessage = new ValidPlaysMessage(validPlays);
		handleMessage(validsMessage);
	}
	
	public void handleCommand(GameCommand command) {
		command.setPlayer(this);
		try {
			PlayValidator validator = game.getValidPlays(this);
			GameState gameState = game.getGameRules().getGameState();
			if(!command.isValidCommand(validator, gameState)) {
				throw new BadCommandException("Command not valid");
			}
			game.handleCommand(command);
		} catch (BadCommandException e) {
			logger.warn("Bad command, kicking: " + e.getMessage());
			disconnect();
		}
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public void disconnect() {
		if(isConnected()) {
			connected = false;
			messageHandler.disconnect();
			messageHandler = null;
		}
	}
	
	public void handleMessage(OutboundMessage message) {
		if(connected && messageHandler != null) {
			logger.debug("Sending " + message + " to " + this);
			messageHandler.handleMessage(message);
		} else {
			logger.warn(this + " not connected");
		}
	}
	@Override
	public String toString() {
		return playerName + "[id:" + getId() + "]";
	}

}
