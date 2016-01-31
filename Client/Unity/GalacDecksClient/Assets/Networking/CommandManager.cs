using UnityEngine;
using System.Collections;
using System;

/// <summary>
/// Validates actions and turns actions into commands to send to the connection, and maintains the
/// current command that we're waiting ack on.
/// </summary>
public class CommandManager : Singleton<CommandManager> {
    
    /// <summary>
    /// If there's a prefab with the name of the 
    /// </summary>
    public PrefabStore commandPrefabs;

    public ICommandFilter commandFilter;

    private int idCounter;
    private CommandBehaviour currentCommand;
    private ValidPlays validPlays;

    /// <summary>
    /// While there's an active command, it can block events from playing out
    /// so they seem like a natural reaction to the command.
    /// </summary>
    public bool BlockingEvents
    {
        get
        {
            if (currentCommand != null) return currentCommand.BlocksEvents;
            return false;
        }
    }

    public ValidPlays ValidPlays
    {
        set
        {
            validPlays = value;
            UIManager.Instance.ResetPingPong();
        }
    }

    /// <summary>
    /// Do we currently have any valid plays?
    /// </summary>
    public bool HasValidPlays()
    {        
        if (validPlays == null) return false;
        return validPlays.ValidPlayCount > 0;
    }

    /// <summary>
    /// Does this entity have any valid actions?
    /// </summary>
    /// <param name="entity"></param>
    /// <returns></returns>
    public bool HasValidPlays(GameEntity entity)
    {
        if (validPlays == null) return false;
        // If we have a command filter, that can override this (in the negative):
        if (commandFilter != null)
        {
            if (!commandFilter.HasValidPlays(entity))
            {
                return false;
            }
        }
        return validPlays.HasValidPlays(entity);
    }

    /// <summary>
    /// Can this entity be played (attack, summon, etc) on the slot?
    /// </summary>
    /// <param name="entity"></param>
    /// <param name="slot"></param>
    /// <returns></returns>
    public bool IsValidPlay(GameEntity entity, UnitSlot slot)
    {
        if (AwaitingAck) return false;
        // If we have a command filter, that can say that some plays are invalid:
        if (commandFilter != null)
        {
            if(!commandFilter.IsValidPlay(entity,slot))
            {
                return false;
            }
        }
        if (validPlays != null) return validPlays.IsValidPlay(entity, slot);
        return false;
    }

    public bool CanEndTurn()
    {
        if (!GameManager.Instance.IsMyTurn) return false;
        if(commandFilter != null)
        {
            return commandFilter.CanEndTurn();
        }
        return true;
    }

    public bool CanOnlyEndTurn()
    {
        if (validPlays == null) return false;
        if (validPlays.ValidPlayCount == 0) return true;
        return false;
    }

    /// <summary>
    /// Can this unit attack whatever's in the target slot?
    /// </summary>
    /// <param name="unit"></param>
    /// <param name="target"></param>
    /// <returns></returns>
    public bool IsValidAttack(UnitEntity unit, UnitEntity target)
    {
        if(unit == null)
        {
            return false;
        }
        if(target == null)
        {
            return false;
        }
        if (!IsValidPlay(unit, target.Slot))
        {
            return false;
        }
        return true;
    }

    public bool IsValidMove(UnitEntity unit, UnitSlot slot)
    {
        if(unit == null)
        {
            return false;
        }
        if (!IsValidPlay(unit, slot))
        {
            return false;
        }
        return true;
    }


    public bool IsValidDiscard(GameEntity entity, UnitSlot slot)
    {
        if (!IsValidPlay(entity, slot)) return false;
        return validPlays.IsDiscardTarget(entity, slot);
    }

    private int NextId
    {
        get
        {
            ++idCounter;
            return idCounter;
        }
    }

    /// <summary>
    /// Are we awaiting the response on a command we sent?
    /// </summary>
    public bool AwaitingAck
    {
        get
        {
            if (currentCommand != null) return true;
            return false;
        }
    }

    public void AckReceived(int id)
    {
        Debug.Log("Received ack id " + id + " for " + currentCommand);
        if(currentCommand != null)
        {
            if (currentCommand.Command.ackId == id)
            {
                currentCommand.Finish();
                currentCommand = null;
            }
            else
            {
                Debug.LogWarning("Received ack id '" + id + "', but didn't match expected '" + currentCommand.Command.ackId + "'");
            }
        }
        
    }

    public bool EndTurn(bool energy, bool mineral, bool draw)
    {
        if (!GameManager.Instance.IsMyTurn) return false;
        GameCommand command = new EndTurnCommand(NextId, energy, mineral, draw);
        SendCommand(command);
        return true;
    }

    /// <summary>
    /// Play a card on a slot, automatically deciding to summon a unit
    /// or play a power on a target.
    /// </summary>
    /// <param name="card"></param>
    /// <param name="slot"></param>
    /// <returns></returns>
    public bool PlayCard(CardEntity card, UnitSlot slot)
    {
        if(card.EntityView.IsUnit)
        {
            return SummonUnit(card, slot);
        }
        else
        {
            if(card.EntityView.RequiresTarget)
            {
                return PlayPower(card, slot);
            }
            else
            {
                return PlayPower(card, null);
            }
        }
    }
    
    /// <summary>
    /// Play a card on an empty slot (summon a unit)
    /// </summary>
    /// <param name="cardData"></param>
    /// <param name="slot"></param>
    public bool SummonUnit(GameEntity card, UnitSlot slot)
    {
        if(!IsValidPlay(card, slot))
        {
            Debug.LogWarning(card + " cannot be played on " + slot);
            return false;
        }
        EntityView cardData = card.EntityView;
        SummonUnitCommand command = new SummonUnitCommand(NextId, cardData, slot);
        SendCommand(command);
        return true;
    }

    /// <summary>
    /// Attack a target with one of our in-play units
    /// </summary>
    /// <param name="attacker"></param>
    /// <param name="target"></param>
    public bool Attack(UnitEntity attacker, UnitEntity target)
    {
        if (!IsValidAttack(attacker, target))
        {
            Debug.Log(attacker + " cannot attack " + target);
            return false;
        }
        EntityView attackUnit = attacker.EntityView;
        EntityView targetUnit = target.EntityView;
        AttackCommand command = new AttackCommand(NextId, attackUnit, targetUnit);
        SendCommand(command);
        return true;
    }

    public bool Move(UnitEntity unit, UnitSlot destination)
    {
        if(!IsValidMove(unit, destination))
        {
            Debug.Log(unit + " cannot move to " + destination);
            return false;
        }
        EntityView moveUnit = unit.EntityView;
        MoveCommand command = new MoveCommand(NextId, moveUnit, destination.x, destination.y);
        SendCommand(command);
        return true;
    }

    public bool PlayPower(CardEntity card, UnitSlot slot)
    {
        if(!IsValidPlay(card, slot))
        {
            Debug.Log("Invalid play");
            return false;
        }
        EntityView cardView = card.EntityView;
        PlayPowerCommand command = new PlayPowerCommand(NextId, cardView, slot);
        SendCommand(command);
        return true;
    }

    public bool Discard(CardEntity card, UnitSlot slot)
    {
        if(!IsValidDiscard(card,slot))
        {
            Debug.Log("Invalid discard");
            return false;
        }
        EntityView cardView = card.EntityView;
        DiscardCommand command = new DiscardCommand(NextId, cardView, slot);
        SendCommand(command);
        return true;
    }

    public bool Concede()
    {
        ConcedeCommand command = new ConcedeCommand(NextId);
        SendCommand(command);
        return true;
    }

    private void SendCommand(GameCommand command)
    {

        if (currentCommand)
        {
            Debug.LogError("Can't send command while awaiting response.");
            return;
        }
        GameObject commandPrefab = null;
        if(command.CommandPrefabName != null)
        {
            commandPrefab = commandPrefabs.GetPrefab(command.CommandPrefabName); 
            if(commandPrefab == null)
            {
                Debug.LogError("Command specified a prefab, but one was not found by that name: " + command.CommandPrefabName);
            }
        }
        if (commandPrefab != null)
        {
            GameObject go = Instantiate(commandPrefab);
            currentCommand = go.GetComponent<CommandBehaviour>();
            go.transform.parent = transform;
            if (currentCommand == null)
            {
                Debug.LogError("Command prefab missing CommandBehaviour");
            }
            currentCommand.Command = command;
        }
        validPlays = null;
        if (commandFilter != null) commandFilter.HandleCommand(command);
        GameClient.Instance.SendRequest(command);
    }

}
