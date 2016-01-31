using UnityEngine;
using System.Collections;

public class Move : TutorialScript {

    public int fromX, fromY;
    public int toX, toY;

    private UnitSlot targetSlot;

    override protected void Activate()
    {
        base.Activate();
        targetSlot = GameManager.Instance.gameBoard.GetSlot(fromX, fromY);
        pointerTarget = targetSlot.gameObject;
    }

    public override bool HasValidPlays(GameEntity entity)
    {
        if (entity.EntityView.InPlay)
        {
            if(entity.EntityView.column == fromX && entity.EntityView.row == fromY) return true;
        }
        return false;
    }

    public override bool IsValidPlay(GameEntity entity, UnitSlot slot)
    {
        if (slot != null && entity != null && entity.EntityView.InPlay)
        {
            if (entity.EntityView.column == fromX && entity.EntityView.row == fromY)
            {
                if(slot.x == toX && slot.y == toY)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public override void HandleCommand(GameCommand command)
    {
        base.HandleCommand(command);
        if (command is MoveCommand)
        {
            IsSatisfied = true;
        }
    }

}
