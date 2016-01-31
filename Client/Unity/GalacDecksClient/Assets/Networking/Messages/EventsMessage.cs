using UnityEngine;
using System.Collections.Generic;

public class EventsMessage {

    public string commandPlayer;
    public int ackId;
    public List<GameEvent> events = new List<GameEvent>();
    public GameView updatedView;

}
