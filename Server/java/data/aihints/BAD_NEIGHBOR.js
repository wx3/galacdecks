// Rule for an entity that damages its neighbors.

if(onSides(me, coordinates)) {
	boardValue -= 3;
}
else {
	boardValue += 3;
}
var c = 0;
getNeighbors(coordinates).forEach(
	function(co) {
		var e = getEntityAt(co);
		if(e != null) {
			debug(e);
			if(e.getOwner() == me) {
				boardValue -= 5;
			} else {
				boardValue += 5;
				debug(e.getOwner() + " != " + me);
			}
			++c;
		}
	}
);

debug(entity + " bad neighbor board value: " + boardValue + " (" + c + " neighbors)");