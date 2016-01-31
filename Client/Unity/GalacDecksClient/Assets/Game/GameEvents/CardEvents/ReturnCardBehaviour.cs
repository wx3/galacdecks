using UnityEngine;
using System.Collections;
using System;

public class ReturnCardBehaviour : GameEventBehaviour {

    public float fadeOut;

    private ReturnCardEvent data;
    private CardEntity card;

    public override GameEvent Data
    {
        set
        {
            data = (ReturnCardEvent)value;
        }
    }

    void Start () {
        card = (CardEntity) GameManager.Instance.GetEntity(data.cardId);
        card.Alpha = 0;
        if(card.EntityView.owner == GameManager.Instance.MyPlayer.playerName)
        {
            GameManager.Instance.playerHand.RemoveCard(card);
        }
        else
        {
            GameManager.Instance.opponentHand.RemoveCard(card);
        }
	}

    override protected void Update()
    {
        base.Update();
    }

    protected override void Remove()
    {
        base.Remove();
        if (card != null)
        {
            Destroy(card.gameObject);
        }
    }

}
