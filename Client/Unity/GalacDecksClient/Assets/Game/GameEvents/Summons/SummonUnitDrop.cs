using UnityEngine;
using System.Collections;

public class SummonUnitDrop :  SummonUnitBehaviour {

    protected override void SpawnUnit()
    {
        hasSpawned = true;
        spawned = GameManager.Instance.SpawnUnit(summoned);
        spawned.transform.position = new Vector3(transform.position.x, transform.position.y + 10, transform.position.z + 100);
        UnitEntity unit = spawned.GetComponent<UnitEntity>();
        unit.transform.localScale = new Vector3(2, 2, 2);
        targetSlot.Unit = unit;
        unit.lerper.SetPosition(unit.NormalPosition, 1f);
        unit.lerper.SetScale(Vector3.one, 1f);
    }
}
