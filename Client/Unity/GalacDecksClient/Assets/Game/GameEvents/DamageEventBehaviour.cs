using UnityEngine;
using UnityEngine.UI;
using System.Collections;
using System;

public class DamageEventBehaviour : GameEventBehaviour {

    private DamageCausedEvent data;
    private UnitEntity unit;
    private CanvasGroup canvasGroup;
    private Text text;

    public override GameEvent Data
    {
        set
        {
            this.data = (DamageCausedEvent)value;
        }
    }

    /*
    void Start () {
        unit = GameManager.Instance.GetEntity(data.entityId) as UnitEntity;
        if (unit == null)
        {
            Debug.LogWarning("Unit for damage missing");
            enabled = false;
            return;
        }

        text = GetComponentInChildren<Text>();
        canvasGroup = GetComponentInChildren<CanvasGroup>();
        text.text = data.amount.ToString();
        transform.position = new Vector3(unit.transform.position.x, unit.transform.position.y + 15, unit.transform.position.z);
	}
	
	// Update is called once per frame
	override protected void Update () {
        base.Update();
        canvasGroup.alpha = Mathf.Lerp(1, 0, elapsed);
        transform.position = transform.position + (new Vector3(0, 0, 10) * Time.deltaTime);
	}
    */
}

