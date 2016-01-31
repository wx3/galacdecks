using UnityEngine;
using System.Collections;

/// <summary>
/// The behaviour displayed when an opponent plays a card: the card is revealed,
/// then removed.
/// </summary>
public class OpponentPlayCardBehaviour : GameEventBehaviour
{

    public float revealDuration = 1.5f;

    private OpponentPlayCardView data;
    private CardEntity card;
    private UnitSlot slot;

    public override GameEvent Data
    {
        set
        {
            this.data = (OpponentPlayCardView)value;
        }
    }

    void Start()
    {
        elapsed = 0;
        card = GameManager.Instance.GetEntity(data.cardView.id) as CardEntity;
        GameManager.Instance.opponentHand.RemoveCard(card);
        if(card == null)
        {
            Debug.LogError("Card not found");
            enabled = false;
        }
        card.EntityView = data.cardView;
        card.lerpTransform.SetTransform(GameManager.Instance.gameBoard.cardReveal, 0.5f);

        if (data.cardView.column >= 0)
        {
            slot = GameManager.Instance.gameBoard.GetSlot(data.cardView.column, data.cardView.row);
        }
    }

    override protected void Update()
    {
        base.Update();
        
        if(card != null)
        {
            if (elapsed > revealDuration)
            {
                card.Alpha = 0;
                
            }
        }
    }

    protected override void Remove()
    {
        base.Remove();
        Destroy(card.gameObject);
    }

}
