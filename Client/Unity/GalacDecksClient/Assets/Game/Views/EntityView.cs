using UnityEngine;
using System.Collections.Generic;

/// <summary>
/// The EntityData corresponds to the latest information we have
/// from the server on an entity.
/// </summary>
// Serializable lets us see it in the inspector:
[System.Serializable]
public class EntityView {

    public int id;
    public bool visible;
    public string name;
    public string owner;
    public string prototype;
    public int row;
    public int column;

    public HashSet<string> tags = new HashSet<string>();
    public Dictionary<string, int> stats = new Dictionary<string, int>();

    public bool OnBoard
    {
        get
        {
            if (row >= 0 && column >= 0) return true;
            return false;
        }
    }

    public bool InHand
    {
        get
        {
            return tags.Contains("IN_HAND");
        }
    }

    public bool InPlay
    {
        get
        {
            return tags.Contains("IN_PLAY");
        }
    }

    public bool IsUnit
    {
        get
        {
            return tags.Contains("UNIT");
        }
    }

    public bool IsPower
    {
        get
        {
            return tags.Contains("POWER");
        }
    }

    public bool IsBlocker
    {
        get
        {
            return tags.Contains("BLOCKER");
        }
    }

    public bool RequiresTarget
    {
        get
        {
            return !tags.Contains("NO_TARGET");
        }
    }

    public int GetAttack()
    {
        return GetStat("ATTACK");
    }

    public int GetMaxHealth()
    {
        return GetStat("MAX_HEALTH");
    }

    public int GetHealth()
    {
        return GetStat("CURRENT_HEALTH");
    }

    public void SetHealth(int health)
    {
        SetStat("CURRENT_HEALTH", health);
    }

    public int GetEnergyCost()
    {
        return GetStat("ENERGY_COST");
    }

    public int GetMineralCost()
    {
        return GetStat("MINERAL_COST");
    }

    public int GetShields()
    {
        return GetStat("CURRENT_SHIELDS");
    }

    public void SetShields(int shields)
    {
        SetStat("CURRENT_SHIELDS", shields);
    }

    public int GetMaxShields()
    {
        return GetStat("MAX_SHIELDS");
    }

    public int GetStat(string stat)
    {
        if (stats == null) return 0;
        if (stats.ContainsKey(stat)) return stats[stat];
        return 0;
    }

    public void SetStat(string stat, int val)
    {
        stats[stat] = val;
    }
}
