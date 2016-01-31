using UnityEngine;
using System.Collections.Generic;

/// <summary>
/// This component is attached to the container for the Player's cards.
/// It handles autopositioning the cards.
/// </summary>
public class PlayerHand : MonoBehaviour {

    public GameObject cardPositionPrefab;
    /// <summary>
    /// Cards are displayed in a "fan" which is a hemi-circle. Their
    /// position is calculated relative to the center of this circle.
    /// </summary>
    public Transform cardFan;
    /// <summary>
    /// How far from the center of the fan is the center of each card?
    /// </summary>
    public float fromFanCenter;
    /// <summary>
    /// How far does the card rise toward the board on selection?
    /// </summary>
    public float selectVerticalRise = 10;
    /// <summary>
    /// How far does the card rise toward the camera on selection?
    /// </summary>
    public float selectDepthRise = 10;
    // When selected, how big do the cards get?
    public float selectScale = 1;

    /// <summary>
    /// What is the normal arc of the hand in degrees?
    /// </summary>
    public float arc = 60;
    /// <summary>
    /// What is the closest and furthest the cards will be apart, in degrees?
    /// </summary>
    public float minSpread = 10;
    public float maxSpread = 30;

    // How far apart is each card in depth from camera?
    public float gap;

    private List<Transform> fanPositions = new List<Transform>();
    private List<Transform> selectPositions = new List<Transform>();
    public List<CardEntity> cards = new List<CardEntity>();

	void Start () {
        RefreshPositions();
        fromFanCenter = Vector3.Distance(transform.position, cardFan.transform.position);
	}
	
	void Update () {
        int removed = cards.RemoveAll(item => item == null);
        if(removed > 0)
        {
            RefreshPositions();
        }
	}

    /// <summary>
    /// Remove existing objects and add cards based on the information in the GameView
    /// </summary>
    /// <param name="view"></param>
    public void InitializeFromView(GameView view, int myPosition)
    {
        PlayerView myPlayer = view.players[myPosition - 1];
        List<CardEntity> cardEntities = new List<CardEntity>();
        foreach(EntityView cardView in myPlayer.hand)
        {
            GameObject go = GameManager.Instance.SpawnCard("Default Card", cardView);
            go.name = cardView.name;
            CardEntity ce = go.GetComponent<CardEntity>();
            if(ce == null)
            {
                Debug.LogError("Card prefab did not have CardEntity component");
            }
            cardEntities.Add(ce);   
        }
        SetCards(cardEntities);
    }

    public void AddCard(CardEntity card)
    {
        cards.Add(card);
        RefreshPositions();
        // Do this after refreshing positions or the lerpattractor will get confused about scale:
        card.transform.parent = cardFan.transform;
    }

    public void RemoveCard(CardEntity card)
    {
        cards.Remove(card);
        RefreshPositions();
    }

    private void SetCards(List<CardEntity> cards)
    {
        foreach(Transform child in cardFan.transform)
        {
            Destroy(child.gameObject);
        }
        this.cards = cards;
        foreach(CardEntity card in cards)
        {
            card.transform.parent = cardFan.transform;
        }
        RefreshPositions();
    }

    private void RefreshPositions()
    {
        int numCards = cards.Count;
        float spread = arc / numCards;
        spread = Mathf.Clamp(spread, minSpread, maxSpread);
        // Angle relative to the fan center for the first card from left to right:
        float angle =  -(((numCards - 1) * spread) / 2);
        float height = 0;
        foreach (Transform pos in fanPositions)
        {
            Destroy(pos.gameObject);
        }
        foreach (Transform pos in selectPositions)
        {
            Destroy(pos.gameObject);
        }
        fanPositions.Clear();
        selectPositions.Clear();
        int i = 0;
        foreach (CardEntity card in cards)
        {
            if(!card.AwaitingAck)
            {
                GameObject fan = new GameObject();
                fan.name = "Fan Position " + i;
                Transform fanPos = fan.GetComponent<Transform>();
                fan.transform.parent = transform;
                float x = Mathf.Cos((angle - 90) * 0.0175f) * fromFanCenter;
                float z = Mathf.Sin((angle - 90) * 0.0175f) * fromFanCenter;
                fanPos.transform.localPosition = new Vector3(cardFan.localPosition.x + x, cardFan.localPosition.y + height, cardFan.localPosition.z - z);
                fanPos.transform.localEulerAngles = new Vector3(0, angle, 0);
                angle += spread;
                height += gap;
                fanPositions.Add(fanPos);
                card.HandTransform = fanPos;
                card.lerpTransform.SetTransform(fanPos, 1f);

                GameObject sel = new GameObject();
                sel.name = "Select Position " + i;
                Transform selPos = sel.GetComponent<Transform>();
                selPos.parent = transform;
                selPos.localPosition = new Vector3(cardFan.localPosition.x + x, cardFan.localPosition.y + height + selectDepthRise, (cardFan.localPosition.z - z) + selectVerticalRise);
                selPos.localScale = new Vector3(selectScale, selectScale, selectScale);
                selPos.localRotation = Quaternion.identity;
                selectPositions.Add(selPos);
                selectPositions.Add(selPos);
                card.SelectTransform = selPos;
                ++i;
            }
        }
    }
}
