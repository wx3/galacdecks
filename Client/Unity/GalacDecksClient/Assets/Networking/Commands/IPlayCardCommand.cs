using UnityEngine;
using System.Collections;

/// <summary>
/// Interface for for commands that play a card, so they can share 
/// CommandBehaviours
/// </summary>
public interface IPlayCardCommand {

	int CardEntityId
    {
        get;
    }
}
