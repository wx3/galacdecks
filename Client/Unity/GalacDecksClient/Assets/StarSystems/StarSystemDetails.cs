using UnityEngine;
using UnityEngine.UI;
using System.Collections;

public class StarSystemDetails : Singleton<StarSystemDetails> {

    public delegate void OnJoinClick(StarSystem starSystem);

    public float tweenDuration;

    public OnJoinClick onJoinClick;

    private StarSystem starSystem;

    private bool shown = false;
    private Vector2 visiblePosition;
    private Vector2 hiddenPosition;
    private RectTransform rectTrans;
    private float tweenTimer = 0;
    private CanvasGroup canvas;

    public StarSystem StarSystem
    {
        get
        {
            return starSystem;
        }
        set
        {
            if (starSystem != value)
            {
                if (starSystem == null)
                {
                    tweenTimer = 0;
                }
                starSystem = value;
                if(starSystem != null) {
                    shown = true;
                } else {
                    shown = false;
                }
            }
        }
    }

    override protected void Awake()
    {
        base.Awake();
        canvas = GetComponent<CanvasGroup>();
        canvas.alpha = 0;
        starSystem = null;
        tweenTimer = 999;
        if (tweenDuration < 0.001f) tweenDuration = 1;
    }

    void Update()
    {
        tweenTimer += Time.deltaTime;
        float amount = tweenTimer / tweenDuration;
        if (shown)
        {
            canvas.alpha = Mathf.Lerp(0, 1, amount);
        }
        else
        {
            canvas.alpha = Mathf.Lerp(1 , 0, amount);
        }
    }

    public void JoinGame()
    {
        if (onJoinClick != null) onJoinClick(starSystem);
        tweenTimer = 0;
        shown = false;
    }

}
