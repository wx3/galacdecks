// At the end of each turn, deal 1 damage to a random neighbor
// trigger: EndTurnEvent


choice = randomUnit(function(e){return areNeighbors(entity,e)});
if(choice != null) {
	dealDamage(entity, choice, 1);
} else{
	debug("No choice");
}
