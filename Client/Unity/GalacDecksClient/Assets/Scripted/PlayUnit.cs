using UnityEngine;
using System.Collections;
using System;

/// <summary>
/// A TutorialScript that wants the player to play a component in a specific slot.
/// </summary>
public class PlayUnit : TutorialScript {

    public int row, col;
    public String prototypeId;

    UnitSlot targetslot;

    public override bool IsTriggered
    {
        get
        {
            return base.IsTriggered;
        }
    }

    override protected void Activate()
    {
        base.Activate();
        targetslot = GameManager.Instance.gameBoard.GetSlot(col, row);
        pointerTarget = targetslot.gameObject;
    }

    public override bool HasValidPlays(GameEntity entity)
    {
        if(entity.PrototypeId == prototypeId)
        {
            return true;
        }
        return false;
    }

    override public bool IsValidPlay(GameEntity entity, UnitSlot slot)
    {
        if(entity.PrototypeId == prototypeId)
        {
            if(slot == targetslot)
            {
                return true;
            }
        }
        return false;
    }

    void Update()
    {
        if(targetslot != null)
        {
            if(targetslot.Unit != null)
            {
                if(targetslot.Unit.EntityView.prototype == prototypeId)
                {
                    IsSatisfied = true;
                }
            }
        }
    }

}
