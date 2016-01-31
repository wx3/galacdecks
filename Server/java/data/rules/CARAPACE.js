// Give all Drones +1 Health
// trigger: PlayCardEvent

var entities = onBoard(function(e){return e.getPrototypeId() == "DRONE"});
entities.forEach(function(e){
	clientEvent("Upgrade", e); 
	addRule(e, "BUFF_HEALTH_1");
	repair(e, 1);
});