using UnityEngine;
using System.Collections;
using System;

public class ShieldRechargeBehaviour : GameEventBehaviour {

    private ShieldRechargeEvent data;

    public override GameEvent Data
    {
        set
        {
            data = (ShieldRechargeEvent)value;
        }
    }

    void Start()
    {
        GameEntity e = GameManager.Instance.GetEntity(data.entityId);
        UnitEntity entity = (UnitEntity)GameManager.Instance.GetEntity(data.entityId);
        entity.EntityView.SetShields(data.newValue);
    }
}
