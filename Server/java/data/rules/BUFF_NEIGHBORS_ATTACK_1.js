// Give friendly neighbors +1 attack
// trigger: BuffPhase

getNeighbors(entity.getCoordinates()).forEach(function(co){
		var e = getEntityAt(co); 
		if(e != null) {
			if(e.getOwner() == entity.getOwner()) {
				e.buffStat("ATTACK", 1);
			}
		}
		
	});
