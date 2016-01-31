using UnityEngine;
using UnityEngine.UI;
using System.Collections;

/// <summary>
/// Displays the player's deck size and 
/// </summary>
public class CardStats : MonoBehaviour {

    public Text valueText;
    public Image cardIcon;
    public float spawnDistance;

    private int _value = 0;
    private int max = 0;

    public int Value
    {
        get
        {
            return _value;
        }
        set
        {
            _value = value;
            RefreshValue();
        }
    }

    public int Max
    {
        get
        {
            return max;
        }
        set
        {
            max = value;
            RefreshValue();
        }
    }

    public Vector3 DrawCard()
    {
        RectTransform rect = cardIcon.GetComponent<RectTransform>();
        Vector3 screenPos = rect.transform.position;
        screenPos.z = spawnDistance;
        Vector3 worldPos = Camera.main.ScreenToWorldPoint(screenPos);
        return worldPos;
    }

    public void RefreshValue()
    {
        valueText.text = _value.ToString();
        if(_value <= 0)
        {
            cardIcon.gameObject.SetActive(false);
        }
        else
        {
            cardIcon.gameObject.SetActive(true);
        }
    }
}
