using UnityEngine;
using System.Collections;

/// <summary>
/// A CommandFilter is a client-side command validator that
/// may restrict commands beyond what the server allows. This
/// is useful for tutorials. It also lets ScriptedDialogs 
/// respond to commands.
/// </summary>
public interface ICommandFilter  {

    bool HasValidPlays(GameEntity entity);
    bool IsValidPlay(GameEntity entity, UnitSlot slot);
    bool CanEndTurn();
    void HandleCommand(GameCommand command);

}
