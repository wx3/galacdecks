using UnityEngine;
using System.Collections;

public class PlayCardEvent : GameEvent {

    /// <summary>
    /// What's the entity just played?
    /// </summary>
    public int cardId;
    public int x, y;
    public int energyCost, mineralCost;
}
