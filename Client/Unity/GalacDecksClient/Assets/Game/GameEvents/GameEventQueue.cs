using UnityEngine;
using System;
using System.Collections.Generic;

/// <summary>
/// The GameEventQueue accepts GameEvents and instantiates their
/// prefab in order when there are no blocking active events.
/// </summary>
public class GameEventQueue : MonoBehaviour {

    public int activeCount;

    private GameView updatedView;
    private ValidPlays updatedPlays;
    private List<GameEventBehaviour> activeEvents = new List<GameEventBehaviour>();
    private Queue<GameEvent> eventQueue = new Queue<GameEvent>();


    /// <summary>
    /// When an updated view is sent by the server, we put it on hold until there are
    /// no blocking events, so stat changes don't happen out of order.
    /// </summary>
    public GameView UpdatedView
    {
        set
        {
            updatedView = value;
        }
    }

    /// <summary>
    /// Same as UpdatedView, the updated valid plays aren't added until we're no longer
    /// blocked.
    /// </summary>
    public ValidPlays UpdatedPlays
    {
        set
        {
            updatedPlays = value;
        }
    }

    /// <summary>
    /// How many pending events are there in the queue.
    /// </summary>
    public int PendingEvents
    {
        get
        {
            return eventQueue.Count;
        }
    }

	void Update () {
        // First remove any null (destroyed) gameobjects:
        activeEvents.RemoveAll(x => x == null);
        activeCount = activeEvents.Count;

        if (IsBlocked())
        {
            return;
        }
        // While there are events in the queue and we haven't spawned any blocking
        // events, dequeue and spawn:
        bool done = false;
        while(!done)
        {
            if(eventQueue.Count > 0)
            {
                GameEvent next = eventQueue.Dequeue();
                GameEventBehaviour eventBehaviour = SpawnEvent(next);
                if (eventBehaviour != null && eventBehaviour.IsBlocking) done = true;
            }
            else
            {
                done = true;
            }
        }
        
        // When the CommandManager isn't blocking, update the valid plays (which allows the player to act):
        if (!CommandManager.Instance.BlockingEvents)
        {
            if (updatedPlays != null)
            {
                CommandManager.Instance.ValidPlays = updatedPlays;
                updatedPlays = null;
            }
        }
        
        // When the GameEventQueue isn't blocking and there are no more events 
        // incoming, update the gameview, which theoretically shouldn't change
        // anything
        if (!IsBlocked() && eventQueue.Count == 0)
        {
            if (updatedView != null)
            {
                GameManager.Instance.UpdateView(updatedView);
                updatedView = null;
            }
        }
        
	}

    public void Add(GameEvent gameEvent)
    {
        if(gameEvent == null)
        {
            throw new System.ArgumentNullException();
        }
        if(gameEvent.prototypeName == null)
        {
            Debug.LogWarning("GameEvent didn't supply a prototypeName");
            return;
        }
        eventQueue.Enqueue(gameEvent);
    }

    public bool IsBlocked()
    {
        if (!GameManager.Instance.IsReady) return true;
        if (CommandManager.Instance.BlockingEvents) return true;
        foreach (GameEventBehaviour ge in activeEvents)
        {
            if (ge.IsBlocking) return true;
        }
        return false;
    }

    private GameEventBehaviour SpawnEvent(GameEvent data)
    {
        GameObject prototype = AssetManager.Instance.GetGameEvent(data.prototypeName);
        if(prototype == null)
        {
            Debug.LogError("Failed to find " + data.prototypeName + " prototype");
            return null;
        }
        GameObject go = Instantiate(prototype);
        GameEventBehaviour eventBehaviour = go.GetComponent<GameEventBehaviour>();
        if(eventBehaviour == null)
        {
            Debug.LogError(go + " was expected to have " + typeof(GameEventBehaviour).ToString());
        }
        eventBehaviour.Data = data;
        activeEvents.Add(eventBehaviour);
        Debug.Log("Adding game event " + eventBehaviour);
        return eventBehaviour;
    }
}
