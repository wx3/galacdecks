using UnityEngine;
using UnityEngine.UI;
using System.Collections;
using System;

/// <summary>
/// PlayableEntity subclass for card which spawn units
/// </summary>
public class CardEntity : GameEntity {

    public LayerMask cardDragLayer;

    public Image attackIcon;
    public Image healthIcon;

    public Text cardNameText;
    public Text cardDescriptionText;
    public Text cardTypeText;
    public Text attackText;
    public Text healthText;
    public Image portrait;

    public int energyCost = -1, mineralCost = -1;
    public Transform resourceContainer;
    public GameObject energyIconPrefab;
    public GameObject mineralIconPrefab;


    public LerpTint glowTint;

    public LerpTransform lerpTransform;

    public Color selectGlow;

    public float selectScale = 1.25f;
    public float dragScale = 0.75f;

    public Transform selectTransform;
    public CanvasGroup flavorCanvasGroup;

    private CanvasGroup canvasGroup;
    private Vector3 dragStart;
    private Transform handTransform;
    private Transform dragProxy;
    private float targetAlpha = 1;

    protected override PrototypeData Prototype
    {
        get
        {
            return base.Prototype;
        }

        set
        {
            base.Prototype = value;
            if(Prototype != null)
            {
                name = Prototype.name;
                portrait.sprite = AssetManager.Instance.GetPortrait(Prototype.portrait);
                cardNameText.text = Prototype.name;
                cardDescriptionText.text = Prototype.description;
                cardTypeText.text = Prototype.TypeString;
                string flavorText = Prototype.flavor;
                if(flavorText.Length > 0)
                {
                    flavorCanvasGroup.GetComponentInChildren<Text>().text = Prototype.flavor;
                    flavorCanvasGroup.gameObject.SetActive(true);
                }
                else
                {
                    flavorCanvasGroup.gameObject.SetActive(false);
                }
                
                
                if(Prototype.tags.Contains("UNIT"))
                {
                    healthIcon.gameObject.SetActive(true);
                    if(Prototype.GetStat("ATTACK") > 0)
                    {
                        attackIcon.gameObject.SetActive(true);
                    }
                    else
                    {
                        attackIcon.gameObject.SetActive(false);
                    }
                }
                else
                {
                    healthIcon.gameObject.SetActive(false);
                    attackIcon.gameObject.SetActive(false);
                }
            }
        }
    }

    public float Alpha
    {
        get
        {
            return targetAlpha;
        }
        set
        {
            targetAlpha = value;
        }
    }

    public Transform HandTransform
    {
        get
        {
            return handTransform;
        }
        set
        {
            handTransform = value;
        }
    }

    public Transform SelectTransform
    {
        get
        {
            return selectTransform;
        }
        set
        {
            selectTransform = value;
        }
    }

    void Awake()
    {
        canvasGroup = GetComponentInChildren<CanvasGroup>();
        lerpTransform = GetComponent<LerpTransform>();
        flavorCanvasGroup.alpha = 0;
    }

    override protected void Start () {
        base.Start();
        
        glowTint.SetColor(Color.black, 0);
	}
    
    void Update()
    {
        RefreshResourceCost();
        RefreshStats();
        if(canvasGroup.alpha > targetAlpha)
        {
            canvasGroup.alpha -= 1f * Time.deltaTime;
        } else if(canvasGroup.alpha < targetAlpha)
        {
            canvasGroup.alpha += 1f * Time.deltaTime;
        }
        if(!IsSelected)
        {
            if(CommandManager.Instance.HasValidPlays(this))
            {
                Color color = Color.Lerp(Color.clear, selectGlow, UIManager.Instance.PingPong);
                glowTint.SetColor(color, 0);
            }
            else
            {
                glowTint.SetColor(Color.clear, 0.1f);
            }
        }
    }

    protected override void Select()
    {
        Debug.Log("Selected " + this);
        base.Select();
        Enlarge();
        glowTint.SetColor(selectGlow, 0.5f);
        RaycastHit hit = GetDragCollision();
        dragStart = hit.point;
    }


    public override void Deselect()
    {
        base.Deselect();
        if (awaitingAck) return;
        glowTint.SetColor(Color.clear, 0.5f);
        Shrink();
        lerpTransform.SetTransform(handTransform, 0.5f);
    }

    /// <summary>
    /// Returns the raycast hit for the mouse against objects in the cardDragLayer
    /// </summary>
    /// <returns></returns>
    private RaycastHit GetDragCollision()
    {
        Ray ray = Camera.main.ScreenPointToRay(Input.mousePosition);
        RaycastHit hit;
        Physics.Raycast(ray, out hit, 1000, cardDragLayer);
        return hit;
    }

    
     public void OnMouseDown()
     {
        if (!IsSelectable) return;
        Select();
        _collider.enabled = false;
        RaycastHit hit = GetDragCollision();
        if(hit.collider != null)
        {
            dragStart = hit.point;
        }
        else
        {
            Debug.Log("Missing drag collision");
        }
     }

    public void OnMouseUp()
    {
        if (!IsSelectable) return;
        UnitSlot slotTarget = UIManager.Instance.SlotTarget;
        if(CommandManager.Instance.IsValidDiscard(this, slotTarget))
        {
            if(CommandManager.Instance.Discard(this, slotTarget))
            {
                Debug.Log("Discarding " + this);
                glowTint.SetColor(Color.clear, 0.7f);
                awaitingAck = true;
            }
            else
            {
                Debug.LogWarning("Failed to discard " + this);
                Deselect();
                _collider.enabled = true;
            }
        }
        else if(!EntityView.RequiresTarget)
        {
            if(UIManager.Instance.OverBoard)
            {
                if (CommandManager.Instance.PlayCard(this, null))
                {
                    //lerpAttractor.SetAttractor(null);
                    glowTint.SetColor(Color.clear, 0.7f);
                    awaitingAck = true;
                }
                else
                {
                    Debug.LogWarning("Failed to play card");
                    Deselect();
                    _collider.enabled = true;
                }
            }
            else
            {
                Deselect();
                _collider.enabled = true;
            }
        }
        else if (slotTarget != null)
        {
            Debug.Log(slotTarget);
            if(CommandManager.Instance.PlayCard(this, slotTarget))
            {
                //lerpAttractor.SetAttractor(null);
                glowTint.SetColor(Color.clear, 0.7f);
                awaitingAck = true;
            }
            else
            {
                Debug.LogWarning("Failed to play card");
                Deselect();
                _collider.enabled = true;
            }
        }
        else
        {
            Deselect();
            _collider.enabled = true;
        }
        UIManager.Instance.dragProxy.transform.localScale = Vector3.one;
    }

    public void OnMouseDrag()
    {
        if (!IsControllable) return;
        RaycastHit hit = GetDragCollision();
        if (hit.collider != null)
        {
            Vector3 pos = hit.point;
            float dist = Vector3.Distance(dragStart, pos);
            if(dist > 5)
            {
                pos.y += 20;
                UIManager.Instance.dragProxy.transform.position = pos;
                UIManager.Instance.dragProxy.transform.rotation = hit.transform.rotation;
                lerpTransform.SetPosition(pos, 0.1f);
                lerpTransform.SetRotation(hit.transform.rotation, 1);
                Shrink();
            }
            
        }

    }

    public override void Remove()
    {
        base.Remove();
        glowTint.SetColor(Color.clear, 0);
    }

    /// <summary>
    /// When the card is first selected, enlarge it and show the flavor/hint text
    /// </summary>
    private void Enlarge()
    {
        lerpTransform.SetTransform(selectTransform, 0.5f);
        flavorCanvasGroup.alpha = 1;
    }

    /// <summary>
    ///  When the card is released or dragged, shrink it back to normal
    /// </summary>
    private void Shrink()
    {
        lerpTransform.SetScale(new Vector3(dragScale, dragScale, dragScale), 1);
        flavorCanvasGroup.alpha = 0;
    }

    /// <summary>
    /// If the resource cost of the entity has changed, update the resource elements on the card or
    /// hide them as appropriate.
    /// </summary>
    private void RefreshResourceCost()
    {
        if(energyCost != EntityView.GetEnergyCost() || mineralCost != EntityView.GetMineralCost())
        {
            foreach (Transform child in resourceContainer.transform)
            {
                Destroy(child.gameObject);
            }
            for (int i = 0; i < EntityView.GetEnergyCost(); i++)
            {
                GameObject go = Instantiate(energyIconPrefab);
                go.transform.SetParent(resourceContainer.transform);
                go.transform.localScale = Vector3.one;
                go.transform.localPosition = Vector3.zero;
                go.transform.localRotation = Quaternion.identity;
            }
            for (int i = 0; i < EntityView.GetMineralCost(); i++)
            {
                GameObject go = Instantiate(mineralIconPrefab);
                go.transform.SetParent(resourceContainer.transform);
                go.transform.localScale = Vector3.one;
                go.transform.localPosition = Vector3.zero;
                go.transform.localRotation = Quaternion.identity;
            }
            energyCost = EntityView.GetEnergyCost();
            mineralCost = EntityView.GetMineralCost();
        }
        
    }

    private void RefreshStats()
    {
        if(EntityView.IsUnit)
        {
            attackText.text = EntityView.GetAttack().ToString();
            healthText.text = EntityView.GetMaxHealth().ToString();
        }
    }

}
