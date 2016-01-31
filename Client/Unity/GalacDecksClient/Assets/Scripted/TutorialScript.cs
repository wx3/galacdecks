using UnityEngine;
using System.Collections;

/// <summary>
/// Base class for tutorial scripts, implements command filtering to block
/// some commands.
/// </summary>
public class TutorialScript : ScriptedDialog, ICommandFilter {

    /// <summary>
    /// If false, no command is valid while this dialog is up. 
    /// </summary>
    public bool allowCommands;

    /// <summary>
    /// If false, the player cannot end their turn while this dialog is up.
    /// </summary>
    public bool allowEndTurn = false;

    /// <summary>
    /// Don't trigger before this turn
    /// </summary>
    public int minTurn;

    public virtual void HandleCommand(GameCommand command)
    {

    }

    public override bool IsTriggered
    {
        get
        {
            if (!base.IsTriggered) return false;
            if (GameManager.Instance.Turn >= minTurn) return true;
            return false;
        }
    }

    public virtual bool CanEndTurn()
    {
        return allowEndTurn;
    }

    public virtual bool HasValidPlays(GameEntity entity)
    {
        return allowCommands;
    }

    public virtual bool IsValidPlay(GameEntity entity, UnitSlot slot)
    {
        return allowCommands;
    }
}
