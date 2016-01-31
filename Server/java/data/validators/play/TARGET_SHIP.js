// Target must be a ship unit in play

if(!target || !target.inPlay() || !target.isUnit() || !target.hasTag("SHIP")) {
	valid = false;
} 
else {
	valid = true;
}