using UnityEngine;
using System.Collections;

/// <summary>
/// A Tutorial that wants the player to play a card on a particular slot.
/// </summary>
public class PlayCard : TutorialScript {

    public int row, col;
    public string prototypeId;

    private UnitSlot targetSlot; 

    public override bool IsTriggered
    {
        get
        {
            return base.IsTriggered;
        }
    }

    override protected void Activate()
    {
        targetSlot = GameManager.Instance.gameBoard.GetSlot(col, row);
        pointerTarget = targetSlot.gameObject;
    }

    public override bool HasValidPlays(GameEntity entity)
    {
        if (entity.PrototypeId == prototypeId)
        {
            return true;
        }
        return false;
    }

    override public bool IsValidPlay(GameEntity entity, UnitSlot slot)
    {
        if (entity.PrototypeId == prototypeId)
        {
            if (slot == targetSlot)
            {
                return true;
            }
        }
        return false;
    }

    public override void HandleCommand(GameCommand command)
    {
        base.HandleCommand(command);
        IsSatisfied = true;
    }

}
