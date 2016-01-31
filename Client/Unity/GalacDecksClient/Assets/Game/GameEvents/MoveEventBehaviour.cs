using UnityEngine;
using System.Collections;
using System;

public class MoveEventBehaviour : GameEventBehaviour
{
    public float moveTime = 0.5f;

    private MoveEvent data;
    private UnitEntity unit;

    public override GameEvent Data
    {
        set
        {
            this.data = (MoveEvent)value;
        }
    }

    void Start()
    {
        this.unit = GameManager.Instance.GetEntity(data.entityId).GetComponent<UnitEntity>();
        this.unit.AwaitingAck = false;
        this.unit.Deselect();
        UnitSlot slot = GameManager.Instance.gameBoard.GetSlot(data.x, data.y);
        slot.Unit = unit;
        unit.lerper.SetPosition(slot.transform.position, moveTime);
        Debug.Log(unit + " is moving to " + slot.x + "," + slot.y);
    }
}
