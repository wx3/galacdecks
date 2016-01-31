// Deal neighbors of attack 1 splash damage.
// trigger: AttackEvent

if(event.attacker == entity) {
	getNeighbors(event.target.getCoordinates()).forEach(function(co){var e = getEntityAt(co); if(e != null) {dealDamage(entity, e, 1, "Simple Damage");}});
}
