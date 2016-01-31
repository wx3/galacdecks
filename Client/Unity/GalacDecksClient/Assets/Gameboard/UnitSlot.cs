using UnityEngine;
using UnityEngine.EventSystems;
using System.Collections.Generic;
using System;

public class UnitSlot : MonoBehaviour {

    public int y;
    public int x;

    // Reference to the UI outline image object:
    public UnityEngine.UI.Image outlineObject;
    // Reference to the glow object:
    public LerpTint glowLerp;
    // Reference to stroke object:
    public LerpTint strokeLerp;

    /// <summary>
    /// The "normal" transform target for units in this slot:
    /// </summary>
    public Transform normalTransform;

    public UnityEngine.UI.Text debugText;

    public Color actionsRemaining;

    public Color neutralStroke;
    public Color friendlyStroke;
    public Color enemyStroke;

    public Color neutralIdle;
    public Color friendlyIdle;
    public Color enemyIdle;

    public Color neutralValid;
    public Color friendlyValid;
    public Color enemyValid;

    public Color neutralFocus;
    public Color friendlyFocus;
    public Color enemyFocus;

    public Color discardStroke;
    public Color discardValid;
    public Color discardFocus;

    public float duration = 1;

    // The unit that's occupying this slot:
    public UnitEntity unit;


    private float timer = 0;

    public UnitEntity Unit
    {
        get
        {
            return unit;
        }
        set
        {
            if (unit != null && value != null)
            {
                Debug.LogWarning("Replacing " + unit + ", that shouldn't happen.");
            }
            unit = value;
            // Set the unit for this slot.
            if(unit != null)
            {
                unit.Slot = this;
                unit.transform.parent = transform;
                //GetComponent<Collider>().enabled = false;
            } 
            else
            {
                //GetComponent<Collider>().enabled = true;
            }
        }
    }


    void Start () {
        glowLerp.SetColor(Color.clear, 0);
        debugText.text = (x + "," + y);
    }
	
	void Update () {
        // If there's a selected entity and this is a valid slot for the entity to play on, light up:
        bool isSelectionFocus = false;
        bool isValidTarget = false;
        bool isValidDiscard = false;
        Color glowColor = neutralIdle;
        Color strokeColor = neutralStroke;
        if (UIManager.Instance.Selected)
        {
            isValidTarget = CommandManager.Instance.IsValidPlay(UIManager.Instance.Selected, this);
            isValidDiscard = CommandManager.Instance.IsValidDiscard(UIManager.Instance.Selected, this);
            if (UIManager.Instance.SlotTarget == this) isSelectionFocus = true;
        }
        if(isValidDiscard)
        {
            strokeColor = discardStroke;
            if (isSelectionFocus) glowColor = discardFocus;
            else glowColor = discardValid;
        }
        else if(Unit && Unit.IsFriendly)
        {
            strokeColor = friendlyStroke;
            if (isSelectionFocus) glowColor = friendlyFocus;
            else if (isValidTarget) glowColor = friendlyValid;
            else glowColor = friendlyIdle;
        }
        else if(Unit && Unit.IsEnemy)
        {
            strokeColor = enemyStroke;
            if (isSelectionFocus) glowColor = enemyFocus;
            else if (isValidTarget) glowColor = enemyValid;
            else glowColor = enemyIdle;
        }
        else
        {
            if (isSelectionFocus) glowColor = neutralFocus;
            else if (isValidTarget) glowColor = neutralValid;
            else glowColor = neutralIdle;
        }
        if(!UIManager.Instance.Selected && Unit != null && Unit.HasValidActions)
        {
            Color c = Color.Lerp(glowColor, actionsRemaining, UIManager.Instance.PingPong);
            glowLerp.SetColor(c, 0f);
            strokeLerp.SetColor(strokeColor, 0f);   
        }
        else
        {
            glowLerp.SetColor(glowColor, duration);
            strokeLerp.SetColor(strokeColor, duration);
        }
        if (GameManager.Instance.gameBoard.showCoordinates)
        {
            debugText.gameObject.SetActive(true);
        }
        else
        {
            debugText.gameObject.SetActive(false);
        }
        
    }


    public override string ToString()
    {
        return "Slot " + x + "x" + y;
    }

}
