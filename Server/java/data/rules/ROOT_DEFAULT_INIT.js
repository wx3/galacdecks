// The default game rules
// trigger: StartGameEvent

var p1 = getGameState().getPlayer(1);
var p2 = getGameState().getPlayer(2);


for(var i = 0; i < 4; i++) {
	playerDrawCard(p1);
	playerDrawCard(p2);
}
playerDrawCard(p2);


p1.setHomeWorld(summonUnit(p1, "HOMEWORLD_1", 4, 4));
p2.setHomeWorld(summonUnit(p2, "HOMEWORLD_2", 4, 0));

summonUnit("PLANET_GREEN", 1, 1);
summonUnit("PLANET_RED", 7, 2);

debug("Game started");