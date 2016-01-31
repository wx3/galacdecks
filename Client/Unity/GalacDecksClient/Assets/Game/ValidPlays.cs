using UnityEngine;
using System.Collections.Generic;

/// <summary>
/// The server tells us all actions that are available to us, which
/// is used for providing player feedback such as highlighting valid
/// cards, moves, etc.
/// </summary>
public class ValidPlays {

    /// <summary>
    /// Collection of in-hand units and the coordinates they can be summoned to, provided
    /// by the server.
    /// </summary>
    public Dictionary<int, List<EntityCoords>> validSummons;
    /// <summary>
    /// Collection of non-unit cards and the coordinates they can be played on.
    /// </summary>
    public Dictionary<int, List<EntityCoords>> validPowers;
    /// <summary>
    /// Collection of powers that can be played with no target.
    /// </summary>
    public List<int> validNoTarget;
    ///
    public Dictionary<int, List<EntityCoords>> validAttacks;
    public Dictionary<int, List<EntityCoords>> validMoves;
    public Dictionary<int, List<EntityCoords>> validDiscards;

    // Cache of GameEntity ids and their valid unit slots
    private Dictionary<int, HashSet<UnitSlot>> validPlays = null;

    // Cache of GameEntity ids (cards) that can be played with no target:
    private HashSet<int> validNoTargetPlays;

    /// <summary>
    /// Do we have any valid plays (apart from End Turn)?
    /// </summary>
    /// <returns></returns>
    public int ValidPlayCount
    {
        get
        {
            if (validPlays == null) precalcValidPlays();
            return validPlays.Keys.Count;
        }
    }

    // Every time we get a new ValidPlays message, this is a new object. The first time
    // we're asked whether a play is valid, we'll look up all valid plays and cache
    // that information.
    private void precalcValidPlays()
    {
        validPlays = new Dictionary<int, HashSet<UnitSlot>>();
        validNoTargetPlays = new HashSet<int>();
        foreach(KeyValuePair<int, List<EntityCoords>> pair in validSummons)
        {
            validPlays[pair.Key] = new HashSet<UnitSlot>();
            foreach(EntityCoords coord in pair.Value)
            {
                UnitSlot slot = GameManager.Instance.gameBoard.GetSlot(coord.x, coord.y);
                validPlays[pair.Key].Add(slot);
            }  
        }
        foreach (KeyValuePair<int, List<EntityCoords>> pair in validPowers)
        {
            validPlays[pair.Key] = new HashSet<UnitSlot>();
            foreach (EntityCoords coord in pair.Value)
            {
                UnitSlot slot = GameManager.Instance.gameBoard.GetSlot(coord.x, coord.y);
                validPlays[pair.Key].Add(slot);
            }
        }
        foreach (KeyValuePair<int, List<EntityCoords>> pair in validAttacks)
        {
            if(!validPlays.ContainsKey(pair.Key))
            {
                validPlays[pair.Key] = new HashSet<UnitSlot>();
            }
            foreach (EntityCoords coord in pair.Value)
            {
                UnitSlot slot = GameManager.Instance.gameBoard.GetSlot(coord.x, coord.y);
                validPlays[pair.Key].Add(slot);
            }
        }
        foreach (KeyValuePair<int, List<EntityCoords>> pair in validMoves)
        {
            if (!validPlays.ContainsKey(pair.Key))
            {
                validPlays[pair.Key] = new HashSet<UnitSlot>();
            }
            foreach (EntityCoords coord in pair.Value)
            {
                UnitSlot slot = GameManager.Instance.gameBoard.GetSlot(coord.x, coord.y);
                validPlays[pair.Key].Add(slot);
            }
        }
        foreach (KeyValuePair<int, List<EntityCoords>> pair in validDiscards)
        {
            if (!validPlays.ContainsKey(pair.Key))
            {
                validPlays[pair.Key] = new HashSet<UnitSlot>();
            }
            foreach (EntityCoords coord in pair.Value)
            {
                UnitSlot slot = GameManager.Instance.gameBoard.GetSlot(coord.x, coord.y);
                validPlays[pair.Key].Add(slot);
            }
        }
        Debug.Log(validDiscards.Count + " valid discards");
        foreach (int entityId in validNoTarget)
        {
            validNoTargetPlays.Add(entityId);
        }
    }

    /// <summary>
    /// Can this entity be played on the supplied slot? If the slot is null,
    /// can this entity be played without a target?
    /// </summary>
    /// <param name="entity"></param>
    /// <param name="slot"></param>
    /// <returns></returns>
    public bool IsValidPlay(GameEntity entity, UnitSlot slot)
    {
        if (validPlays == null) precalcValidPlays();
        if (slot == null)
        {
            return validNoTargetPlays.Contains(entity.EntityId);
        }
        if (validPlays.ContainsKey(entity.EntityId))
        {
            return validPlays[entity.EntityId].Contains(slot);
        }
        return false;
    }

    public bool IsDiscardTarget(GameEntity entity, UnitSlot slot)
    {
        if (slot == null) return false;
        if(validDiscards.ContainsKey(entity.EntityId))
        {
            foreach(EntityCoords coord in validDiscards[entity.EntityId])
            {
                if(coord.x == slot.x && coord.y == slot.y)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /// <summary>
    /// Does the supplied entity have any valid plays?
    /// </summary>
    /// <param name="entity"></param>
    /// <returns></returns>
    public bool HasValidPlays(GameEntity entity)
    {
        if (validPlays == null) precalcValidPlays();
        if (validPlays.ContainsKey(entity.EntityId))
        {
            if(validPlays[entity.EntityId].Count > 0)
            {
                return true;
            }
        }
        if(validNoTargetPlays.Contains(entity.EntityId))
        {
            return true;
        }
        return false;
    }

	public class EntityCoords
    {
        public int x, y;
    }
}
