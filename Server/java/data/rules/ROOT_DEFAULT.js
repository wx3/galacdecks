// The default game rules
// trigger: StartGameEvent

var p1 = getGameState().getPlayer(0);
var p2 = getGameState().getPlayer(1);

addDeck(p1, "MULLIGAN");
addDeck(p2, "MULLIGAN");

for(var i = 0; i < 4; i++) {
	playerDrawCard(p1);
	playerDrawCard(p2);
}

p1.setHomeWorld(summonUnit(p1, "HOMEWORLD_1", 2, 3));
p2.setHomeWorld(summonUnit(p2, "HOMEWORLD_2", 2, 0));