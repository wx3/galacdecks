using UnityEngine;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System.Collections.Generic;

/// <summary>
/// Turns Json tokens from the server into their appropriate GameEvents
/// </summary>
public class EventFactory {

    public static GameEvent FromJToken(JToken token)
    {
        GameEvent gameEvent = null;
        string eventClass = (string)token["eventClass"];
        if(string.IsNullOrEmpty(eventClass))
        {
            throw new System.Exception("eventClass missing from JToken");
        }
        switch (eventClass)
        {
            case "EndTurnEvent":
                gameEvent = token.ToObject<EndTurnEvent>();
                break;
            case "DrawCardView":
                gameEvent = token.ToObject<DrawCardView>();
                break;
            case "ReturnCardEvent":
                gameEvent = token.ToObject<ReturnCardEvent>();
                break;
            case "UpdateResourceEvent":
                gameEvent = token.ToObject<UpdateResourceEvent>();
                break;
            case "ConsumeResourceEvent":
                gameEvent = token.ToObject<ConsumeResourceEvent>();
                break;
            case "PlayCardEvent":
                gameEvent = token.ToObject<PlayCardEvent>();
                break;
            case "DiscardEvent":
                gameEvent = token.ToObject<DiscardEvent>();
                break;
            case "OpponentPlayCardView":
                gameEvent = token.ToObject<OpponentPlayCardView>();
                break;
            case "SummonUnitView":
                gameEvent = token.ToObject<SummonUnitView>();
                break;
            case "MoveEvent":
                gameEvent = token.ToObject<MoveEvent>();
                break;
            case "DamageCausedEvent":
                gameEvent = token.ToObject<DamageCausedEvent>();
                break;
            case "ShieldRechargeEvent":
                gameEvent = token.ToObject<ShieldRechargeEvent>();
                break;
            case "DeathEvent":
                gameEvent = token.ToObject<DeathEvent>();
                break;
            case "GameOverEvent":
                gameEvent = token.ToObject<GameOverEvent>();
                break;
            case "ClientPrefabEvent":
                gameEvent = token.ToObject<ClientPrefabEvent>();
                break;
            default:
                Debug.Log("Unknown event class: " + eventClass + ", ignoring");
                break;
        }
        return gameEvent;
    }

}
