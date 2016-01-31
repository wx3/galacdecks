// Disable an entity for 1 full turn (2 half turns)
// trigger: PlayCardEvent

clientEvent("Arc Zap", event.target); 
event.target.disable(2);