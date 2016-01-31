// The default game rules
// trigger: StartGameEvent

var p1 = getGameState().getPlayer(0);
var p2 = getGameState().getPlayer(1);

// Create the player's deck:
for(var i = 0; i < 3; i++) {
	addDeck(p1, "MISSILE");
	addDeck(p1, "PULSE_CANNON");
	addDeck(p1, "ION_GUN");
	addDeck(p1, "ION_GUN");
	addDeck(p1, "REINFORCED_HULL");
	addDeck(p1, "ION_GUN");
}

for(var i = 0; i < 2; i++) {
	addDeck(p2, "ION_GUN");
	addDeck(p2, "ION_GUN");
	addDeck(p2, "ION_GUN");
	addDeck(p2, "REINFORCED_HULL");
	addDeck(p2, "ION_GUN");
	addDeck(p2, "BASIC_TURRET");
	addDeck(p2, "REINFORCED_HULL");
	addDeck(p2, "BASIC_TURRET");
	addDeck(p2, "BASIC_TURRET");
	addDeck(p2, "BASIC_TURRET");
	addDeck(p2, "BASIC_TURRET");
}

for(var i = 0; i < 4; i++) {
	playerDrawCard(p1);
	playerDrawCard(p2);
}


p1.setHomeWorld(summonUnit(p1, "HOMEWORLD_1", 0, 2));
p2.setHomeWorld(summonUnit(p2, "TUTORIAL_ENEMY_1", 5, 2));
debug("Game started");