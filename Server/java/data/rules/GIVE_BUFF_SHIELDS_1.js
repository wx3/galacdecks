// Give a unit +1 Shields
// trigger: PlayCardEvent

addRule(event.target, "BUFF_SHIELDS_1");
rechargeShields(event.target, 1);