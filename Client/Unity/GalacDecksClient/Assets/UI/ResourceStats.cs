using UnityEngine;
using UnityEngine.UI;
using System.Collections;

public class ResourceStats : MonoBehaviour {

    const int MAX = 10;

    public Text valueText;
    public string resource;
    public GameObject iconPrefab;
    public LayoutGroup iconContainer;
    /// <summary>
    /// How far from the camera should the resources spawn when deducted?
    /// </summary>
    public float resourceSpawnDistance;

    public Sprite activeSprite;
    public Sprite inactiveSprite;

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
            RefreshIcons();
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
            RefreshIcons();
        }
    }

    /// <summary>
    /// Deduct one from the resource and return the worldspace coordinates where the 
    /// resource fly-in should spawn.
    /// </summary>
    /// <returns></returns>
    public Vector3 DeductResource()
    {
        Value = Value - 1;
        if(Value < 0)
        {
            Debug.LogWarning("Value should not be negative!");
        }
        RectTransform rect = iconContainer.transform.GetChild(MAX - (Value + 1)).gameObject.GetComponent<RectTransform>();
        Vector3 screenPos = rect.transform.position;
        screenPos.z = resourceSpawnDistance;
        Vector3 worldPos = Camera.main.ScreenToWorldPoint(screenPos);
        return worldPos;
    }

	void Start () {
        foreach(Transform child in iconContainer.transform)
        {
            Destroy(child.gameObject);
        }
        for(int i = 0; i < MAX; i++)
        {
            GameObject go = Instantiate(iconPrefab);
            go.transform.SetParent(iconContainer.transform);
        }
	}
	
	// Update is called once per frame
	void Update () {
        
        if (GameManager.Instance.MyPlayer == null) return;
    }

    public void RefreshIcons()
    {
        valueText.text = Value + " / " + Max;
        for (int i = 0; i < MAX; i++)
        {
            GameObject go = iconContainer.transform.GetChild(MAX - (i + 1)).gameObject;
            Image image = go.GetComponent<Image>();
            if (i < Value)
            {
                image.sprite = activeSprite;
            } 
            else if(i < Max)
            {
                image.sprite = inactiveSprite;
            }
            else
            {
                image.sprite = null;
            }
        }
    }
}
