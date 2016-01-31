using UnityEngine;
using System.Collections;
using System;

public class DeathEventBehaviour : GameEventBehaviour
{
    private DeathEvent data;
    private UnitEntity unit;

    public override GameEvent Data
    {
        set
        {
            this.data = (DeathEvent)value;
        }
    }

    /*
    public override bool IsBlocking
    {
        get
        {
            if (base.IsBlocking) return true;
            if (unit != null) return true;
            return base.IsBlocking;
        }
    }
    */

    void Start()
    {
        unit = (UnitEntity)GameManager.Instance.GetEntity(data.entityId);
        transform.position = unit.transform.position;
        unit.Remove();
        if(unit.deathPrefab != null)
        {
            Instantiate(unit.deathPrefab, unit.transform.position, Quaternion.identity);
        }
    }

    override protected void Update()
    {
        base.Update();
        if(unit != null)
        {
            Destroy(unit.gameObject);
        }
    }
}
