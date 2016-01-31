// Deal remaining energy after cost as damage.
// trigger: PlayCardEvent

var remaining = getEnergy(owner);
consumeEnergy(entity, remaining);
dealDamage(entity,event.target,remaining,"Reactor Burst");

