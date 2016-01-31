// Surround a coordinate with drones
// trigger: PlayCardEvent

getNeighbors(event.target.getCoordinates()).forEach(function(co){
	var e = getEntityAt(co); 
	if(e == null) {
		summonUnit(owner, "DRONE", co);
	}
});