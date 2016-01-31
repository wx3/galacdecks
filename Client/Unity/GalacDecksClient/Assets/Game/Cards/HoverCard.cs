using UnityEngine;
using UnityEngine.UI;
using System.Collections;

public class HoverCard : MonoBehaviour {

    public Text nameText;
    public Image portrait;
    public Text descriptionText;
    public Text typeText;

    public Transform resourceContainer;
    public GameObject energyIconPrefab;
    public GameObject mineralIconPrefab;

    public Text attackText;
    public Text healthText;
    public Transform flavorContainer;
    public CanvasGroup canvasGroup;
    public float revealTime;

    public float offset;
    public float elevation;

    private RectTransform rectTrans;
    private PrototypeData prototype;
    private GameEntity entity;
    private float elapsed = 0;
    private bool visible = false;

    public PrototypeData Prototype
    {
        get
        {
            return prototype;
        }
        set
        {
            prototype = value;
            if(prototype != null)
            {
                portrait.sprite = AssetManager.Instance.GetPortrait(Prototype.portrait);
                nameText.text = prototype.name;
                descriptionText.text = prototype.description;
                typeText.text = prototype.TypeString;
                attackText.text = prototype.GetStat("ATTACK").ToString();
                healthText.text = prototype.GetStat("MAX_HEALTH").ToString();
                if(prototype.flavor.Length > 0)
                {
                    flavorContainer.gameObject.SetActive(true);
                    flavorContainer.GetComponentInChildren<Text>().text = prototype.flavor;
                }
                else
                {
                    flavorContainer.gameObject.SetActive(false);
                }
                RefreshResources();                
                
            }
        }
    }

    public bool Visible
    {
        get
        {
            return visible;
        }
    }

    void Start()
    {
        elapsed = 100;
        canvasGroup.alpha = 0;
        rectTrans = GetComponent<RectTransform>();
    }

    public void Show(GameEntity entity)
    {
        this.entity = entity;
        elapsed = 0;
        visible = true;
        Vector3 pos = entity.transform.position;
        pos.y += elevation;
        pos.z = 0;
        if (entity.transform.position.x < 0)
        {
            pos.x += offset;
        } else
        {
            
            pos.x -= offset;
        }
        transform.position = pos;
    }

    private void RefreshResources()
    {
        foreach (Transform child in resourceContainer.transform)
        {
            Destroy(child.gameObject);
        }
        for (int i = 0; i < prototype.GetStat("ENERGY_COST"); i++)
        {
            GameObject go = Instantiate(energyIconPrefab);
            go.transform.SetParent(resourceContainer.transform);
            go.transform.localScale = Vector3.one;
            go.transform.localPosition = Vector3.zero;
            go.transform.localRotation = Quaternion.identity;
        }
        for (int i = 0; i < prototype.GetStat("MINERAL_COST"); i++)
        {
            GameObject go = Instantiate(mineralIconPrefab);
            go.transform.SetParent(resourceContainer.transform);
            go.transform.localScale = Vector3.one;
            go.transform.localPosition = Vector3.zero;
            go.transform.localRotation = Quaternion.identity;
        }
    }

    public void Hide()
    {
        elapsed = 0;
        visible = false;
    }

    void Update()
    {
        elapsed += Time.deltaTime;
        if(visible)
        {
            canvasGroup.alpha = Mathf.Lerp(0, 1, elapsed / revealTime);
        } else
        {
            canvasGroup.alpha = Mathf.Lerp(1, 0, elapsed / revealTime);
        }
    }
}
