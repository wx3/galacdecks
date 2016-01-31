using UnityEngine;
using System.Collections;

/// <summary>
/// A GameEventBehaviour is a component that will play out the client-side result
/// of a server generated game event. 
/// 
/// </summary>
public abstract class GameEventBehaviour : MonoBehaviour {

    /// <summary>
    /// The default behaviour for GameEventBehaviours is to
    /// stop blocking after a delay (can be zero).
    /// </summary>
    public float blockDelay = 0;

    protected float elapsed = 0;
    public float autoDestroyAfter = 2f;

    /// <summary>
    /// A blocking event prevents additional events from starting.
    /// </summary>
	public virtual bool IsBlocking {
        get
        {
            if (gameObject == null) return false;
            if (elapsed >= blockDelay) return false;
            return true;
        }
    }

    /// <summary>
    /// Subtypes of GameEventBehaviour should cast the data to their
    /// appropriate type (we could solve this with generics but 
    /// Unity doesn't support generic components).
    /// </summary>
    public abstract GameEvent Data
    {
        set;
    }

    protected virtual void Update()
    {
        elapsed += Time.deltaTime;
        if (elapsed > autoDestroyAfter) Remove();
    }

    protected virtual void Remove()
    {
        Destroy(gameObject);
    }
}
