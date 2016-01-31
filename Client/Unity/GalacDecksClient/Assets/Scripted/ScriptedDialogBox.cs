using UnityEngine;
using UnityEngine.UI;
using System.Collections;
using System;

/// <summary>
/// Shows the player scripted dialog messages in response to game events.
/// </summary>
public class ScriptedDialogBox : MonoBehaviour, ICommandFilter {

    public HelpArrow helpArrow;
    public float fadeTime = 1;

    public DialogContainer friendlyDialog;
    public DialogContainer enemyDialog;

    /// <summary>
    /// Transform container for active scripts.
    /// </summary>
    public Transform activeScripts;

    private CanvasGroup canvasGroup;
    private ScriptedDialog activeDialog;

    private float delayTimer;

    /// <summary>
    /// If true, rejects all commands not accepted by a ScriptedDialog implementing ICommandFilter.
    /// </summary>
    private bool tutorialMode = false;

    public ScriptedDialog ActiveDialog
    {
        get
        {
            return activeDialog;
        }
        set
        {
            if(activeDialog != null)
            {
                activeDialog.IsActiveDialog = false;
            }
            activeDialog = value;
            if(value != null)
            {
                activeDialog.IsActiveDialog = true;
                if(activeDialog.isFriendly)
                {
                    friendlyDialog.Dialog = activeDialog;
                    friendlyDialog.gameObject.SetActive(true);
                    enemyDialog.gameObject.SetActive(false);
                }
                else
                {
                    enemyDialog.Dialog = activeDialog;
                    enemyDialog.gameObject.SetActive(true);
                    friendlyDialog.gameObject.SetActive(false);
                }
                if (activeDialog is ICommandFilter)
                {
                    CommandManager.Instance.commandFilter = (ICommandFilter)activeDialog;
                } else
                {
                    CommandManager.Instance.commandFilter = null;
                }
            }
        }
    }

    public bool IsTutorialMode
    {
        get {
            return tutorialMode;
        }
        set {
            tutorialMode = value;
        }
    }

    void Awake()
    {
        canvasGroup = GetComponent<CanvasGroup>();
        canvasGroup.alpha = 0;
        CommandManager.Instance.commandFilter = this;
    }

    void Update()
    {
        if(canvasGroup.alpha == 0)
        {
            canvasGroup.blocksRaycasts = false;
        }
        else
        {
            canvasGroup.blocksRaycasts = true;
        }
        if(activeScripts == null)
        {
            return;
        }
        if(activeDialog == null)
        {
            delayTimer += Time.deltaTime;
            foreach (Transform child in activeScripts)
            {
                ScriptedDialog dialog = child.GetComponent<ScriptedDialog>();
                if (dialog.IsTriggered && delayTimer > dialog.minDelay)
                {
                    ActiveDialog = dialog;
                    break;
                }
            }
            if(canvasGroup.alpha > 0)
            {
                canvasGroup.alpha -= Time.deltaTime / fadeTime;
            }
            helpArrow.Target = null;
        }
        else {

            if (canvasGroup.alpha < 1)
            {
                canvasGroup.alpha += Time.deltaTime / fadeTime;
            }
            if (activeDialog.IsSatisfied)
            {
                Destroy(activeDialog.gameObject);
                CommandManager.Instance.commandFilter = null;
                if (activeDialog.nextDialogPrefab != null)
                {
                    GameObject go = Instantiate(activeDialog.nextDialogPrefab);
                    go.transform.SetParent(activeScripts, false);
                    ICommandFilter commandFilter = go.GetComponent<ICommandFilter>();
                    CommandManager.Instance.commandFilter = commandFilter;
                }
                delayTimer = 0;
            }
            helpArrow.Target = activeDialog.PointerTarget;
            
        }
    }

    public void OnDismiss()
    {
        if(ActiveDialog != null)
        {
            ActiveDialog.Dismiss();
        }
    }

    public bool HasValidPlays(GameEntity entity)
    {
        if(activeDialog != null && activeDialog is ICommandFilter)
        {
            return ((ICommandFilter)activeDialog).HasValidPlays(entity);
        }
        return !tutorialMode;
    }

    public bool IsValidPlay(GameEntity entity, UnitSlot slot)
    {
        if (activeDialog != null && activeDialog is ICommandFilter)
        {
            return ((ICommandFilter)activeDialog).IsValidPlay(entity, slot);
        }
        return !tutorialMode;
    }

    public bool CanEndTurn()
    {
        if (activeDialog != null && activeDialog is ICommandFilter)
        {
            return ((ICommandFilter)activeDialog).CanEndTurn();
        }
        return !tutorialMode;
    }

    public void HandleCommand(GameCommand command)
    {
        if (activeDialog != null && activeDialog is ICommandFilter)
        {
            ((ICommandFilter)activeDialog).HandleCommand(command);
        }
    }
}
