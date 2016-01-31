// When ship is attacked, deal 1 damage to attacker
// trigger: AttackEvent

if(event.target.owner == entity.owner){dealDamage(entity, event.attacker, 1, "Default Attack");}