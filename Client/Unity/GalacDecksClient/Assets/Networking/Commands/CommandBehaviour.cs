using UnityEngine;
using System.Collections;

/// <summary>
/// A CommandBehaviour handles any "warm up" effect that occurs between
/// when a player executes an action and the server sends the events
/// that occurred in order to hide the inherent latency.
/// </summary>
public abstract class CommandBehaviour : MonoBehaviour {

    public float minimumBlockTime = 0;

    protected bool finished = false;
    protected float timeSinceStart = 0;
    protected float timeSinceFinish = 0;

    public abstract GameCommand Command
    {
        set;
        get;
    }

    /// <summary>
    /// Like events, commands can also block events from playing
    /// until they've done their minimum warm up.
    /// </summary>
    public virtual bool BlocksEvents
    {
        get
        {
            return timeSinceStart < minimumBlockTime;
        }
    }

    protected virtual void Update()
    {
        timeSinceStart += Time.deltaTime;
        if(finished)
        {
            timeSinceFinish += Time.deltaTime;
        }
    }

    /// <summary>
    /// Finish is called when the command is acknowledged.
    /// </summary>
    public virtual void Finish()
    {
        finished = true;
    }
    
}
