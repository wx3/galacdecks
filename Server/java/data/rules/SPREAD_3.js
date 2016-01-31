// Deal 3 damages randomly split among enemies
// trigger: PlayCardEvent

for(i = 0; i < 3; i++) {
	choice = randomUnit(function(e){return entity.enemyOf(e)});
	if(choice != null) {
		dealDamage(entity, choice, 1, "Proton Scatter");
	}
	else{
		debug("No choice");
	}
}

