// Target must be unit and player must have > 0 energy left.


if(!target || !target.inPlay() || !target.isUnit() || (getEnergy(owner) < 1) || behindCover(target)) {
	valid = false;
} 
else {
	valid = true;
}
