// Target any unblocked enemy

if(!target || !target.inPlay() || !target.isUnit() ||  behindCover(target)) {
	valid = false;
} 
else {
	valid = true;
}
