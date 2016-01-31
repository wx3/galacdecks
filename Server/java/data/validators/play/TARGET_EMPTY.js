// Target any empty coordinate

if(isOnBoard(coord) && (getEntityAt(coord) == null)) {
	valid = true;
} else {
	valid = false;
}
