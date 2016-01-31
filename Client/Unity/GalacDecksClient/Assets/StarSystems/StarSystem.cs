using UnityEngine;
using UnityEngine.UI;
using System.Collections.Generic;
using UnityEngine.EventSystems;

public class StarSystem : Selectable {

    public GameObject sun;
    public Image line;
    public Text nameText;
    public Text descriptionText;
    public StarSystemDetails starSystemsDetails;

    public float selectionDuration;

    public StarSelectorState normal;
    [SerializeField]
    public StarSelectorState highlighted;
    [SerializeField]
    public StarSelectorState pressed;
    [SerializeField]
    public StarSelectorState disabled;

    [SerializeField]
    public StarSystemData data;

    private StarSystemsCamera starCam;
    private ProFlare flare;
    private float normalScale;
    private float normalDistance;
    private CanvasGroup canvas;

    private StarSelectorState previousState;
    private StarSelectorState state;
    private float stateTimer;
    
    [System.Serializable]
    public class StarSelectorState
    {
        public float canvasAlpha = 1;
        public Color lineColor = Color.white;
        public Color titleColor = Color.white;
    }

    private StarSelectorState GetSelectorState(SelectionState state)
    {
        if (state == SelectionState.Disabled) return disabled;
        else if (state == SelectionState.Highlighted) return highlighted;
        else if (state == SelectionState.Pressed) return pressed;
        return normal;
    }

    override protected void Awake()
    {
        base.Awake();
        canvas = GetComponentInChildren<CanvasGroup>();
        state = normal;
        previousState = normal;
    }

	override protected void Start () {
        base.Start();
        if(data != null)
        {
            nameText.text = data.name;
            descriptionText.text = data.description;
        }
        flare = GetComponentInChildren<ProFlare>();
        if(flare != null)
        {
            normalScale = flare.GlobalScale;
            normalDistance = Vector3.Distance(transform.position, Camera.main.transform.position);
        }
        starCam = Camera.main.GetComponent<StarSystemsCamera>();
        canvas.alpha = 0.5f;
	}

    protected override void DoStateTransition(SelectionState state, bool instant)
    {
        base.DoStateTransition(state, instant);
        StarSelectorState sState = GetSelectorState(state);
        if(this.state != sState)
        {
            previousState = this.state;
            this.state = sState;
            stateTimer = 0;
        }
    }

    void Update()
    {
        if(flare != null)
        {
            if(starCam != null && starCam.TargetSystem == this)
            {
                float dist = Vector3.Distance(transform.position, Camera.main.transform.position);
                float amount = 1 - (dist / normalDistance);
                amount = amount * amount * amount * amount;
                float scale = Mathf.Lerp(normalScale, starCam.peakFlare, amount);
                flare.GlobalScale = scale;
            }
        }
        stateTimer += Time.deltaTime;
        float v = 1;
        if(selectionDuration > 0)
        {
            v = stateTimer / selectionDuration;
        }

        if(canvas != null)
        {
            canvas.alpha = Mathf.Lerp(previousState.canvasAlpha, state.canvasAlpha, v);
            line.color = Color.Lerp(previousState.lineColor, state.lineColor, v);
            nameText.color = Color.Lerp(previousState.titleColor, state.titleColor, v);
        }

    }

    public override void OnSelect(BaseEventData eventData)
    {
        base.OnSelect(eventData);
        if (starSystemsDetails != null)
        {
            starSystemsDetails.StarSystem = this;
        }
    }

    public override void OnDeselect(BaseEventData eventData)
    {
        base.OnDeselect(eventData);
        canvas.alpha = 0.5f;
    }


}
