using UnityEngine;
using System.Collections;

/// <summary>
/// Unlike the PlayCardEvent, this contains the view of the card, since the player doesn't
/// already know this information.
/// </summary>
public class OpponentPlayCardView : GameEvent
{
    public EntityView cardView;
}
