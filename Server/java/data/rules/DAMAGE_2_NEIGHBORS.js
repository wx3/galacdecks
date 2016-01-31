// Deal 2 damage to all neighbors
// trigger: PlayCardEvent

getNeighbors(event.target.getCoordinates()).forEach(function(co){var e = getEntityAt(co); if(e != null) {dealDamage(event.target, e, 2);}});