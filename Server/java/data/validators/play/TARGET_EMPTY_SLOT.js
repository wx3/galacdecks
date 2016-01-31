// Target any empty slot

if(isSlot(coord) && (getEntityAt(coord) == null)) {
	valid = true;
} else {
	valid = false;
}
