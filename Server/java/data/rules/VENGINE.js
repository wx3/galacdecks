// Whenever owner's homeworld is damaged, deal 1 damage to a random enemy for each damage taken.
// trigger: DamageTakenEvent

if(event.victim == getHomeworld(entity.getOwner())) {
	for(i = 0; i < event.damage.damageTaken; i++) {
		choice = randomUnit(function(e){return entity.enemyOf(e)});
		if(choice != null) {
			dealDamage(entity, choice, 1);}else{Debug("No choice")
		}
	}
}
