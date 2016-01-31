using UnityEngine;
using System.Collections;

/// <summary>
/// A scripted dialog is dialog content that appears in response to GameEvents.
/// It can be dismissed manually, or automatically in response to changes in
/// game state.
/// </summary>
public class ScriptedDialog : MonoBehaviour {

    public string title;
    [TextArea(3, 10)]
    public string text;

    /// <summary>
    /// Does this dialog appear in the friendly or opposition space?
    /// </summary>
    public bool isFriendly = true;

    /// <summary>
    /// If defined, the this will automatically be spawned when this dialog is 
    /// dismissed.
    /// </summary>
    public GameObject nextDialogPrefab;

    /// <summary>
    /// If false, the player cannot dismiss this script manually.
    /// </summary>
    public bool allowDismiss = true;

    /// <summary>
    /// Won't appear until the GameEvent queue is clear.
    /// </summary>
    public bool waitForEvents = true;

    /// <summary>
    /// Minimum time between this and the last dialog.
    /// </summary>
    public float minDelay;

    private bool isSatisfied;
    private bool isActiveDialog;
    protected GameObject pointerTarget;

    public bool IsSatisfied
    {
        get
        {
            return isSatisfied;
        }
        set
        {
            isSatisfied = value;
        }
    }

    public virtual bool IsTriggered
    {
        get
        {
            if (waitForEvents)
            {
                if (GameManager.Instance.gameEventQueue.IsBlocked() && GameManager.Instance.gameEventQueue.PendingEvents > 0)
                {
                    return false;
                }
            }
            return true;
        }
    }

    public bool IsActiveDialog
    {
        get
        {
            return isActiveDialog;
        }
        set
        {
            if(isActiveDialog != value)
            {
                isActiveDialog = value;
                if (IsActiveDialog) Activate();
            }
        }
    }

    public GameObject PointerTarget
    {
        get
        {
            return pointerTarget;
        }
    }

    public void Dismiss()
    {
        isSatisfied = true;
    }
	
    /// <summary>
    /// Called when this is first made the active dialog
    /// </summary>
    protected virtual void Activate()
    {
        if (minDelay < 0.1f) minDelay = 0.1f;
    }

}
