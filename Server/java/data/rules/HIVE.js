// Summon a drone in a random neighbor slot
// trigger: EndTurnEvent

coord = randomCoordinates(function(c){
	if(getEntityAt(c) != null) return false;
	return areNeighbors(coord,c);
});
if(coord != null) {
	summonUnit(owner, "DRONE", coord);
}
