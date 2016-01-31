// Target must be a weapon in play

if(!target || !target.inPlay() || !target.isUnit() || !target.hasTag("WEAPON")) {
	valid = false;
} 
else {
	valid = true;
}