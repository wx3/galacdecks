using UnityEngine;
using System.Collections.Generic;

/// <summary>
/// Base class for all event behaviours that deal damage from one entity to one or 
/// more other entities.
/// </summary>
public class BaseDamageEventBehaviour : GameEventBehaviour {

    protected DamageCausedEvent data;
    protected UnitEntity cause;
    protected List<GameEntity> targets = new List<GameEntity>();

    public override GameEvent Data
    {
        set
        {
            data = (DamageCausedEvent)value;
            GameEntity attacker = GameManager.Instance.GetEntity(data.attackerId);
            // If the attacking entity is a card, the cause will be the owner's homeworld:
            if(attacker is CardEntity)
            {
                cause = (UnitEntity)GameManager.Instance.GetHomeworld(attacker.EntityView.owner);
            }
            else 
            {
                cause = (UnitEntity)attacker;
            }
            foreach (DamageEffect damage in data.damages)
            {
                GameEntity target = GameManager.Instance.GetEntity(damage.entityId);
                targets.Add(target);
            }

        }
    }
}
