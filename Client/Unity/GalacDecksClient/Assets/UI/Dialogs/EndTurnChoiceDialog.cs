using UnityEngine;
using UnityEngine.UI;
using System.Collections;

/// <summary>
/// Prompt the player to make their end of turn choice for energy/mineral/draw.
/// This component should probably be refactored into a more general "choice" dialog
/// for future cases where players are presented with choices.
/// </summary>
public class EndTurnChoiceDialog : MonoBehaviour {

    public Image energyCheck;
    public Image mineralCheck;
    public Image drawCheck;

    public Button energyChoice;
    public Button mineralChoice;
    public Button drawChoice;

    public Button okButton;

    private float transitionTimer;
    private bool closed = false;
    private CanvasGroup canvasGroup;
    private RectTransform rectTransform;
    private AudioSource audioSource;

    void Awake()
    {
        canvasGroup = GetComponent<CanvasGroup>();
        canvasGroup.alpha = 0;
        rectTransform = GetComponent<RectTransform>();
        audioSource = GetComponent<AudioSource>();
    }

	void Start () {
        energyCheck.enabled = false;
        mineralCheck.enabled = false;
        drawCheck.enabled = false;
        rectTransform.anchoredPosition = Vector2.zero;
        UIManager.Instance.CanvasBlocked = true;
	}
	
	void Update () {
        transitionTimer += Time.deltaTime;
        if (!closed)
        {
            canvasGroup.alpha = Mathf.Lerp(0, 1, transitionTimer);
        }
        else
        {
            canvasGroup.alpha = Mathf.Lerp(1, 0, transitionTimer);
            if (canvasGroup.alpha <= 0)
            {
                Destroy(gameObject);
            }
        }
        int selected = 0;
        if (energyCheck.enabled) ++selected;
        if (mineralCheck.enabled) ++selected;
        if (drawCheck.enabled) ++selected;
        if(selected == 1)
        {
            okButton.interactable = true;
        }
        else
        {
            okButton.interactable = false;
        }
	}

    void SelectionChange()
    {
        audioSource.Play();
    }

    public void ToggleEnergy()
    {
        SelectionChange();
        energyCheck.enabled = !energyCheck.enabled;
        if(energyCheck.enabled)
        {
            if (mineralCheck.enabled) mineralCheck.enabled = false;
            if (drawCheck.enabled) drawCheck.enabled = false;
        }
    }

    public void ToggleMineral()
    {
        SelectionChange();
        mineralCheck.enabled = !mineralCheck.enabled;
        if (mineralCheck.enabled)
        {
            if (energyCheck.enabled) energyCheck.enabled = false;
            if (drawCheck.enabled) drawCheck.enabled = false;
        }
    }

    public void ToggleDraw()
    {
        SelectionChange();
        drawCheck.enabled = !drawCheck.enabled;
        if (drawCheck.enabled)
        {
            if (energyCheck.enabled) energyCheck.enabled = false;
            if (mineralCheck.enabled) mineralCheck.enabled = false;
        }
    }

    public void Cancel()
    {
        closed = true;
        transitionTimer = 0;
        UIManager.Instance.CanvasBlocked = false;
    }

    public void EndTurn()
    {
        bool energy = energyCheck.enabled;
        bool mineral = mineralCheck.enabled;
        bool draw = drawCheck.enabled;
        CommandManager.Instance.EndTurn(energy, mineral, draw);
        closed = true;
        transitionTimer = 0;
        UIManager.Instance.CanvasBlocked = false;
    }
}
