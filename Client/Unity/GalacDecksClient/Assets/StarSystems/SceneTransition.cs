using UnityEngine;
using UnityEngine.UI;
using System.Collections;

/// <summary>
/// Smooths the transition between scenes with a background image and message.
/// </summary>
public class SceneTransition : MonoBehaviour {

    public CanvasGroup canvas;
    public Text text;
    public float startDelay;
    public float transitionDuration;

    private float elapsed = 999;
    private bool show = true;

    public string Message
    {
        set
        {
            text.text = value;
        }
    }

    void Awake()
    {
        canvas.alpha = 1;
        canvas.gameObject.SetActive(true);
        if (transitionDuration <= 0) transitionDuration = 1;
        Message = "";
    }

    public void TransitionOut()
    {
        show = true;
        elapsed = 0;
    }

    public void TransitionIn()
    {
        show = false;
        elapsed = 0;
        canvas.alpha = 1;
        canvas.gameObject.SetActive(true);
    }

	void Update () {
        elapsed += Time.deltaTime;
        if(elapsed > startDelay)
        {
            if(show)
            {
                canvas.alpha = Mathf.Lerp(0, 1, (elapsed - startDelay) / transitionDuration);
            }
            else
            {
                canvas.alpha = Mathf.Lerp(1, 0, (elapsed - startDelay) / transitionDuration);
            }
        }
        if(canvas.alpha > 0)
        {
            canvas.gameObject.SetActive(true);
        } else
        {
            canvas.gameObject.SetActive(false);
        }
	}
}
