using UnityEngine;
using System.Collections;
using System;

public class DrawOpponentBehaviour : GameEventBehaviour {

    public Vector3 startSize;
    private DrawCardView data;

    public override GameEvent Data
    {
        set
        {
            data = (DrawCardView)value;
        }
    }

    void Start()
    {
        GameObject go = GameManager.Instance.SpawnCard("Default Card", data.card);
        go.transform.position = UIManager.Instance.enemyCardStats.DrawCard();
        go.transform.localEulerAngles = new Vector3(0, 0, 180);
        go.transform.localScale = startSize;
        CardEntity ce = go.GetComponent<CardEntity>();
        ce.EntityView = data.card;
        if (ce == null)
        {
            Debug.LogError("Card prefab did not have CardEntity component");
        }
        GameManager.Instance.opponentHand.AddCard(ce);
        GameManager.Instance.OpponentPlayer.deckSize = data.deckSize;
    }

    override protected void Update()
    {
        base.Update();
    }
}
