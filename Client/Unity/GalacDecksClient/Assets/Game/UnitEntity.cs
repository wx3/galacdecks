using UnityEngine;
using UnityEngine.UI;
using System.Collections.Generic;
using System;

/// <summary>
/// A UnitEntity is an in-play entity that can be targeted and can
/// target other entities for attack.
/// </summary>
public class UnitEntity : GameEntity {

    public Image attackIcon;
    public Text attackText;
    public Text healthText;
    public Image shieldIcon;
    public Text shieldText;
    public Renderer unitImage;
    public Shields shields;
    public Color friendlyColor;
    public Color enemyColor;
    public LerpTint ownerTint;

    public float attackHeight;
    public float blockerHeight;

    public LerpTransform lerper;

    public Transform visualContainer;

    /// <summary>
    /// Prefab to instantiate for entity death.
    /// </summary>
    public GameObject deathPrefab;

    public GameObject damageCounterPrefab;

    private UnitSlot slot;
    private Vector3 startDrag;

    protected override PrototypeData Prototype
    {
        get
        {
            return base.Prototype;
        }

        set
        {
            base.Prototype = value;
            if (Prototype != null && unitImage != null) {
                Texture2D texture = AssetManager.Instance.GetHexPortraitTexture(Prototype.portrait);
                unitImage.material.mainTexture = texture;
                unitImage.material.shaderKeywords = new string[1] { "_EMISSION" };
                unitImage.material.SetTexture("_EMISSION", texture);
            }
        }
    }

    public bool HasValidActions
    {
        get
        {
            return CommandManager.Instance.HasValidPlays(this);
        }
    }

    public UnitSlot Slot
    {
        get
        {
            return slot;
        }
        set
        {
            if(slot != null)
            {
                slot.Unit = null;
            }
            slot = value;
        }
    }

    void Awake()
    {
        lerper = GetComponent<LerpTransform>();
    }

    protected void Update()
    {
        if(EntityView != null)
        {
            if (attackText != null)
            {
                if(EntityView.GetAttack() > 0)
                {
                    attackText.text = EntityView.GetAttack().ToString();
                    attackIcon.gameObject.SetActive(true);
                }
                else
                {
                    attackIcon.gameObject.SetActive(false);
                }
            }
            if (healthText != null)
            {
                healthText.text = EntityView.GetHealth().ToString();
            }

            if(shieldIcon != null)
            {
                if(EntityView.GetMaxShields() > 0)
                {
                    shieldIcon.gameObject.SetActive(true);
                }
                else
                {
                    shieldIcon.gameObject.SetActive(false);
                }
            }
            if(ownerTint != null)
            {
                if(IsFriendly)
                {
                    ownerTint.SetColor(friendlyColor, 0.5f);
                } else
                {
                    ownerTint.SetColor(enemyColor, 0.5f);
                }
            }
            
            if(shieldText != null)
            {
                shieldText.text = EntityView.GetShields().ToString();
            }
            if(shields != null)
            {
                shields.Strength = EntityView.GetShields();
            }
        }    
    }

    public void dealDamage(DamageEffect damage, GameObject cause = null)
    {
        EntityView.SetHealth(EntityView.GetHealth() - damage.damageTaken);
        EntityView.SetShields(EntityView.GetShields() - damage.shieldBlocked);
        if(damage.damageTaken > 0)
        {
            GameObject go = Instantiate(damageCounterPrefab);
            go.transform.position = new Vector3(transform.position.x, transform.position.y + 15, transform.position.z);
            DamageCounter counter = go.GetComponent<DamageCounter>();
            counter.Damage = damage.damageTaken;
        }
        if(shields != null && damage.shieldBlocked > 0)
        {
            shields.ShieldHit(damage);
        }
    }

    public Vector3 NormalPosition
    {
        get
        {
            if (slot == null)
            {
                return Vector3.zero;
            }
            else
            {
                Vector3 pos = slot.transform.position;
                if (EntityView.IsBlocker)
                {
                    pos.y += blockerHeight;
                }
                return pos;
            }
        }
    }

    protected override void Select()
    {
        base.Select();
        if (!IsSelectable) return;
        UIManager.Instance.ShowHoverCard(this);

        if(IsControllable && HasValidActions)
        {
            Vector3 origin = new Vector3(transform.position.x, transform.position.y, transform.position.z);
            UIManager.Instance.dragCursor.StartDrag(transform.position);
            UIManager.Instance.dragCursor.Color = Color.white;
        }
    }

    public override void Deselect()
    {
        base.Deselect();
        UIManager.Instance.dragCursor.EndDrag();
        UIManager.Instance.HideHoverCard();
    }

    public void OnMouseDown()
    {
        if (!IsSelectable) return;
        if(!IsSelected)
        {
            Select();
            startDrag = UIManager.Instance.MousePosition;
            //_collider.enabled = false;
        }
        else
        {
            Deselect();
        }
        
    }

    public void OnMouseDrag()
    {
        float dist = Vector3.Distance(UIManager.Instance.MousePosition, startDrag);
        if(dist > 25)
        {
            UIManager.Instance.HideHoverCard();
        }
    }

    public override void OnMouseOver()
    {
        
    }

    public override void OnMouseOut()
    {
        UIManager.Instance.HideHoverCard();
        Debug.Log("MouseOut");
    }

    public void OnMouseUp()
    {
        if (!IsSelectable) return;
        UnitSlot slotTarget = UIManager.Instance.SlotTarget;
        if(slotTarget != null)
        {
            if(slotTarget.Unit != null)
            {
                CommandManager.Instance.Attack(this, slotTarget.Unit);
                Deselect();
            }
            else
            {
                if(CommandManager.Instance.IsValidMove(this, slotTarget))
                {
                    CommandManager.Instance.Move(this, slotTarget);
                }
                Deselect();
            }
        } else
        {
            Deselect();
        }
        _collider.enabled = true;
        
    }

    public override void Remove()
    {
        base.Remove();
        if(slot != null)
        {
            slot.Unit = null;
        }
    }

}