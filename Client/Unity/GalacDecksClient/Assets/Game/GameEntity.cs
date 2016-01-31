using UnityEngine;
using UnityEngine.EventSystems;
using System.Collections;
using System;

/// <summary>
/// A GameEntity component is attached to gameobjects which are
/// the client side representation of a server entity.
/// </summary>
[DisallowMultipleComponent]
public abstract class GameEntity : MonoBehaviour {

    public EntityView entityData;
    private bool selected;
    protected Collider _collider;
    private PrototypeData prototype;
    protected bool awaitingAck;

    [SerializeField]
    public EntityView EntityView
    {
        get
        {
            return entityData;
        }
        set
        {
            entityData = value;
            if (!string.IsNullOrEmpty(value.prototype))
            {
                Prototype = GameManager.Instance.GetPrototype(value.prototype);
            }
        }
    }

    protected virtual PrototypeData Prototype
    {
        get
        {
            return prototype;
        }
        set
        {
            prototype = value;
        }
    }

    public int EntityId
    {
        get
        {
            return entityData.id;
        }
    }

    public string PrototypeId
    {
        get
        {
            if(prototype != null) return prototype.id;
            return null;
        }
    }

    public bool IsSelectable
    {
        get
        {
            if (UIManager.Instance.CanvasBlocked) return false;
            return true;
        }
    }

    public bool IsControllable
    {
        get
        {
            if (!IsSelectable) return false;
            if (entityData.owner != GameManager.Instance.playerName) return false;
            if (!GameManager.Instance.IsMyTurn) return false;
            return true;
        }
    }

    public bool IsSelected
    {
        get
        {
            return selected;
        }
    }

    public bool IsFriendly
    {
        get
        {
            if(entityData.owner == GameManager.Instance.gameView.viewer)
            {
                return true;
            }
            return false;
        }
    }

    public bool IsEnemy
    {
        get
        {
            if(entityData.owner == GameManager.Instance.OpponentPlayer.playerName)
            {
                return true;
            }
            return false;
        }
    }

    /// <summary>
    /// After an entity has been released on a target, it awaits acknowledgment 
    /// from the server.
    /// </summary>
    public bool AwaitingAck
    {
        get
        {
            return awaitingAck;
        }
        set
        {
            awaitingAck = value;
        }
    }


    public bool IsValidSlotTarget(UnitSlot slot)
    {
        return CommandManager.Instance.IsValidPlay(this, slot);
    }

    virtual protected void Start()
    {
        if(entityData == null)
        {
            Debug.LogWarning(this + " missing EntityData");
        }
        _collider = GetComponent<Collider>();
        if(_collider == null)
        {
            Debug.LogWarning(this + " won't work without a collider");
        }
    }

    virtual public void Hover()
    {

    }

    virtual public void Dehover()
    {

    }

    virtual protected void Select()
    {
        if (IsSelected) return;
        selected = true;
        UIManager.Instance.Selected = this;
    }

    virtual public void Deselect()
    {
        if(IsSelected)
        {
            selected = false;
            if(UIManager.Instance.Selected == this)
            {
                UIManager.Instance.Selected = null;
            }
        }
    }

    virtual public void OnMouseOver()
    {
        if (!IsSelectable) return;
        Hover();
    }

    virtual public void OnMouseOut()
    {
        Dehover();
    }

    //abstract public void OnMouseDown();
    //abstract public void OnMouseUp();
    //abstract public void OnMouseDrag();

    virtual protected bool CanTarget(GameObject go)
    {
        return false;
    }

    virtual public void Remove()
    {
        if (IsSelected) UIManager.Instance.Selected = null;
        GameManager.Instance.RemoveEntity(EntityId);
    }

    public override string ToString()
    {
        if(entityData == null)
        {
            return name + " (No Data)";
        } else
        {
            return name + "(" + entityData.name + "." + entityData.id + ")";
        }
    }
}
