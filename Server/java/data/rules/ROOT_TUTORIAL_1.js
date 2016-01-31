// Tutorial #1 Game Init
// trigger: StartGameEvent

var p1 = getGameState().getPlayer(1);
var p2 = getGameState().getPlayer(2);

// Create the player's deck:
for(var i = 0; i < 4; i++) {
	addDeck(p1, "DESTROYER");
	addDeck(p1, "FIGHTER");
	addDeck(p1, "MISSILE");
	addDeck(p1, "UPGRADE");
	addDeck(p1, "FIGHTER");
	addDeck(p1, "FIGHTER");
	addDeck(p1, "FIGHTER");
}

// Enemy deck:
for(var i = 0; i < 3; i++) {
	addDeck(p2, "DRONE");
	addDeck(p2, "DRONE");
	addDeck(p2, "DRONE");
	addDeck(p2, "DRONE");
	addDeck(p2, "DRONE");
	addDeck(p2, "DRONE");
	addDeck(p2, "DRAW_2");
}

for(var i = 0; i < 3; i++) {
	playerDrawCard(p1);
	playerDrawCard(p2);
}


p1.setHomeWorld(summonUnit(p1, "HOMEWORLD_1", 4, 4));
p2.setHomeWorld(summonUnit(p2, "TUTORIAL_ENEMY_1", 4, 0));


debug("Game started");