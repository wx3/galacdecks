using UnityEngine;
using UnityEngine.UI;
using System.Collections;

public class EndTurnButton : MonoBehaviour {

    public Image glow;
    public GameObject endTurnChoiceDialogPrefab;

    private Color normalGlow;

    UnityEngine.UI.Button button;
    Text text;
    CanvasGroup canvasGroup;

    void Start()
    {
        button = GetComponent<UnityEngine.UI.Button>();
        text = GetComponentInChildren<Text>();
        canvasGroup = GetComponent<CanvasGroup>();
        normalGlow = glow.color;
    }
	
	void Update () {
	    if(CommandManager.Instance.CanEndTurn())
        {
            button.interactable = true;
            canvasGroup.alpha = 1;
            // Only show the pulse if that's the only option:
            if(CommandManager.Instance.CanOnlyEndTurn())
            {
                glow.enabled = true;
                glow.color = Color.Lerp(Color.clear, normalGlow, UIManager.Instance.PingPong);
            }
            else
            {
                glow.enabled = false;
            }
        }
        else
        {
            button.interactable = false;
            canvasGroup.alpha = 0.5f;
            glow.enabled = false;
        }
        if(GameManager.Instance.IsMyTurn)
        {
            text.text = "End Turn";
        }
        else
        {
            text.text = "Enemy Turn";
        }
	}

    public void EndTurn()
    {
        GameObject go = Instantiate(endTurnChoiceDialogPrefab);
        go.transform.SetParent(GameManager.Instance.dialogContainer);
        
    }
}
