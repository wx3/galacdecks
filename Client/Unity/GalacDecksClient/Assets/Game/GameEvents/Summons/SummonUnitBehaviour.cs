using UnityEngine;
using System.Collections.Generic;
using System;

public class SummonUnitBehaviour : GameEventBehaviour {

    public float delay;
    /// <summary>
    /// GameObject to instantiate when the unit arrives at its destination:
    /// </summary>
    public GameObject summonImpactPrefab;
    protected GameObject summonImpactInstance;

    protected UnitEntity spawned = null;
    protected EntityView summoned;
    protected UnitSlot targetSlot;

    private SummonUnitView data;

    private ParticleSystem[] particleSystems;
    protected bool started = false;
    protected bool hasSpawned = false;

    public override GameEvent Data
    {
        set
        {
            this.data = (SummonUnitView) value;
            summoned = data.entityView;
        }
    }

    protected virtual void Start () {
        targetSlot = GameManager.Instance.gameBoard.GetSlot(summoned.column, summoned.row);
        particleSystems = GetComponentsInChildren<ParticleSystem>();
        transform.position = targetSlot.transform.position;
    }

    protected override void Update()
    {
        base.Update();
        if (elapsed > delay && !hasSpawned)
        {
            SpawnUnit();

        }
        if (!started)
        {
            foreach (ParticleSystem system in particleSystems)
            {
                if (system.particleCount > 0) started = true;
            }
        }
        if (started && spawned != null)
        {
            int count = 0;
            foreach (ParticleSystem system in particleSystems)
            {
                count += system.particleCount;
            }
            if (count == 0)
            {
                Destroy(gameObject);
            }
        }
        if (spawned != null && summonImpactPrefab != null && summonImpactInstance == null)
        {
            ImpactEffect();
        }
    }
    
    protected virtual void ImpactEffect()
    {
        if (Vector3.Distance(spawned.transform.position, targetSlot.normalTransform.position) < 1)
        {
            summonImpactInstance = Instantiate(summonImpactPrefab);
            summonImpactInstance.transform.parent = transform;
            summonImpactInstance.transform.position = targetSlot.normalTransform.position;
        }
    }

    protected virtual void SpawnUnit()
    {
        hasSpawned = true;
        spawned = GameManager.Instance.SpawnUnit(summoned);
        spawned.transform.position = transform.position;
        UnitEntity unit = spawned.GetComponent<UnitEntity>();
        unit.transform.localScale = new Vector3(0, 0, 0);
        targetSlot.Unit = unit;
        unit.lerper.SetScale(Vector3.one, 0.75f);
    }

}
