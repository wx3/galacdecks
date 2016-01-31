// Deal 1 damage to neighboring entities at the end of each turn.
// trigger: EndTurnEvent

clientEvent("Singularity", entity); 
getNeighbors(entity.getCoordinates()).forEach(function(co){var e = getEntityAt(co); if(e != null) {dealDamage(entity, e, 1, "Simple Damage");}});
