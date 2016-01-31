// Tutorial #2 Game Init
// trigger: StartGameEvent

var p1 = getGameState().getPlayer(1);
var p2 = getGameState().getPlayer(2);

// Create the player's deck:
for(var i = 0; i < 4; i++) {
	addDeck(p1, "DESTROYER");
	addDeck(p1, "UPRISING");
	addDeck(p1, "FIGHTER");
	addDeck(p1, "MISSILE");
	addDeck(p1, "UPGRADE");
	addDeck(p1, "PROTON_SPREAD");
	addDeck(p1, "FIGHTER");
}

// Enemy deck:
for(var i = 0; i < 3; i++) {
	addDeck(p2, "DRONE");
	addDeck(p2, "DRONE");
	addDeck(p2, "DRONE");
	addDeck(p2, "DRAW_2");
	addDeck(p2, "DRONE");
	addDeck(p2, "UPGRADE");
	addDeck(p2, "DRONE");
	addDeck(p2, "DRONE");
}

for(var i = 0; i < 3; i++) {
	playerDrawCard(p1);
	playerDrawCard(p2);
}


p1.setHomeWorld(summonUnit(p1, "HOMEWORLD_1", 4, 4));
p2.setHomeWorld(summonUnit(p2, "TUTORIAL_ENEMY_2", 4, 0));

summonUnit("PLANET_BLUE", 1, 1);
summonUnit("PLANET_RED", 7, 2);


debug("Game started");