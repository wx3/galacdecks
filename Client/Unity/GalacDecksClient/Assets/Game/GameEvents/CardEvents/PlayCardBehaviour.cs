using UnityEngine;
using System.Collections.Generic;
using System;

public class PlayCardBehaviour : GameEventBehaviour {

    public float fadeOut = 1;
    public GameObject energyFlyPrefab;
    public GameObject mineralFlyPrefab;
    public float flyDelay;

    private PlayCardEvent data;
    private CardEntity card;

    private int energyCounter = 0;
    private int mineralCounter = 0;
    private float flyTimer = 0;
    private List<ResourceFly> flyIns = new List<ResourceFly>();

    public override GameEvent Data
    {
        set
        {
            this.data = (PlayCardEvent)value;
        }
    }

    void Start () {
        card = GameManager.Instance.GetEntity(data.cardId) as CardEntity;
        if(data.x >= 0)
        {
            UnitSlot slot = GameManager.Instance.gameBoard.GetSlot(data.x, data.y);
        }
        flyTimer = flyDelay;
	}

    protected override void Remove()
    {
        base.Remove();
        if(card != null)
        {
            Destroy(card.gameObject);
        }
    }

    override protected void Update()
    {
        base.Update();
        card.Alpha -= Time.deltaTime / fadeOut;
        if (card.Alpha <= 0)
        {
            Remove();
        }
    }

}
