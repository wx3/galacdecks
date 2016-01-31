// Blast a unit for 4 damage and its neighbors for 2
// trigger: PlayCardEvent

dealDamage(entity, event.target, 4, "Nuke Attack");
getNeighbors(event.target.getCoordinates()).forEach(function(co){var e = getEntityAt(co); if(e != null) {dealDamage(entity, e, 1, "Simple Damage");}});