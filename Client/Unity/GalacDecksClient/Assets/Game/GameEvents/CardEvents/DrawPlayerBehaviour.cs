using UnityEngine;
using System.Collections;
using System;

public class DrawPlayerBehaviour : GameEventBehaviour {

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
        Vector3 pos = UIManager.Instance.cardStats.DrawCard();
        go.transform.position = pos;
        go.transform.localScale = startSize;
        go.transform.localEulerAngles = new Vector3(0, 0, 180);
        CardEntity ce = go.GetComponent<CardEntity>();
        if (ce == null)
        {
            Debug.LogError("Card prefab did not have CardEntity component");
        }
        GameManager.Instance.playerHand.AddCard(ce);
        GameManager.Instance.MyPlayer.deckSize = data.deckSize;
    }

    override protected void Update()
    {
        base.Update();
    }
}
