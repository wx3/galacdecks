using UnityEngine;
using System.Collections;

public class Attack : TutorialScript {

    public int attackerX;
    public int attackerY;

    /// <summary>
    /// If supplied, what kind of target should we be attacking?
    /// </summary>
    public string targetPrototypeId;

    public override bool HasValidPlays(GameEntity entity)
    {
        if (entity.EntityView.InPlay)
        {
            return true;
        }
        return false;
    }

    public override bool IsValidPlay(GameEntity entity, UnitSlot slot)
    {
        if(entity.EntityView.InPlay)
        {
            
            if(targetPrototypeId == "")
            {
                return true;
            }
            if(slot != null && slot.unit != null && slot.unit.PrototypeId == targetPrototypeId)
            {
                return true;
            }
        }
        return false;
    }

    protected override void Activate()
    {
        base.Activate();
        pointerTarget = GameManager.Instance.gameBoard.GetSlot(attackerX, attackerY).gameObject;
    }

    public override void HandleCommand(GameCommand command)
    {
        base.HandleCommand(command);
        if(command is AttackCommand)
        {
            IsSatisfied = true;
        }
    }
}
