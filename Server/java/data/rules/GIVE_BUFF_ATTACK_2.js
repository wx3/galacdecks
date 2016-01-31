// Deal 2 damage to a ship and give it +2 attack
// trigger: PlayCardEvent

clientEvent("Arc Zap", event.target); 
dealDamage(entity, event.target, 2, "Simple Damage");
addRule(event.target, "BUFF_ATTACK_2");