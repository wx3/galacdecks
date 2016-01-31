using UnityEngine;
using System.Collections;

public class DiscardEventBehaviour : GameEventBehaviour {

    public float fadeOut = 1;

    private DiscardEvent data;
    private CardEntity card;


    public override GameEvent Data
    {
        set
        {
            this.data = (DiscardEvent)value;
        }
    }

    void Start()
    {
        card = GameManager.Instance.GetEntity(data.cardId) as CardEntity;
        if (data.x >= 0)
        {
            UnitSlot slot = GameManager.Instance.gameBoard.GetSlot(data.x, data.y);
        }
    }

    protected override void Remove()
    {
        base.Remove();
        if (card != null)
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
