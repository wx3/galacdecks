// Deal 2 damage to all enemies
// trigger: PlayCardEvent

clientEvent("Gamma Burst", getHomeworld(getOpponent())); 
enemyUnits(owner).forEach(function(e){dealDamage(entity, e, 2, "Simple Damage")});

