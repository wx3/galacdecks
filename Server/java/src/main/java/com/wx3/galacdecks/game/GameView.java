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
import java.util.List;

/**
 * A player's view of the game state. This is sent as JSON
 * after a client connects. Individual {@link EntityView}s
 * may hide information from players, such as details on 
 * cards in the other player's hand.
 * 
 * @author Kevin
 *
 */
public class GameView {
	
	String viewer;
	int viewerPosition;
	
	long gameId;
	int turn;
	String currentPlayer;
	List<EntityCoordinates> coordinates;
	List<PlayerView> players = new ArrayList<PlayerView>();
	List<EntityView> inPlay = new ArrayList<EntityView>();

	public GameView(GameState game, PlayerState viewer) {
		this.viewer = viewer.getName();
		this.viewerPosition = viewer.position;
		turn = game.turn;
		currentPlayer = game.getCurrentPlayer().name;
		players.add(new PlayerView(game.getPlayer(1), viewer));
		players.add(new PlayerView(game.getPlayer(2), viewer));
		coordinates = new ArrayList<EntityCoordinates>(game.getAllCoordinates());
		for(GameEntity entity : game.getInPlay()) {
			EntityView entityView = new EntityView(entity, viewer);
			this.inPlay.add(entityView);
		}
	}
	
	public String getViewerName() {
		return viewer;
	}
}
