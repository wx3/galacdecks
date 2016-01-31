// Give an entity +1 attack
// trigger: PlayCardEvent

clientEvent("Upgrade", event.target); 
addRule(event.target, "BUFF_ATTACK_1");