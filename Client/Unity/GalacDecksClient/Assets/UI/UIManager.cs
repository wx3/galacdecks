using UnityEngine;
using UnityEngine.UI;
using System.Collections;

/// <summary>
/// Singleton for player interaction with the game and information display.
/// </summary>
public class UIManager : Singleton<UIManager> {

    public CanvasGroup uiCanvas;

    /// <summary>
    /// A collider used to detect whether the mouse is over the gameboard.
    /// </summary>
    public Collider gameBoardPlane;
    public DragCursor dragCursor;

    public LerpTint canvasBlocker;
    public Color blockTint;

    public LayerMask slotLayer;
    public LayerMask boardLayer;

    public ResourceStats energyStats;
    public ResourceStats mineralStats;
    public CardStats cardStats;

    public CardStats enemyCardStats;

    public Text enemyEnergy;
    public Text enemyMinerals;

    public float selectionElevation;

    private static float RAY_LIMIT = 1000f;
    public GameEntity selected;
    public GameObject dragProxy;

    public HoverCard hoverCard;

    public GameObject quitDialogPrefab;

    public float pingPongFreq = 2;
    private float pingPong;
    private int currentTurn;
    private bool canvasVisible = false;

    // Access modifier to prevent instantiation
	protected UIManager() {}

    public bool CanvasVisible
    {
        get
        {
            return canvasVisible;
        }
        set
        {
            canvasVisible = value;
        }
    }

    public bool CanvasBlocked
    {
        get
        {
            return canvasBlocker.GetComponent<Image>().raycastTarget;
        }
        set
        {
            if(value)
            {
                canvasBlocker.SetColor(blockTint, 0.5f);
                canvasBlocker.GetComponent<Image>().raycastTarget = true;
            }
            else
            {
                canvasBlocker.SetColor(Color.clear, 0.5f);
                canvasBlocker.GetComponent<Image>().raycastTarget = false;
            }
        }
    }

    void Start()
    {
        if (pingPongFreq <= 0) pingPongFreq = 1;
        if(slotLayer.value == 0)
        {
            Debug.LogWarning("UIManager requires a slot layer for slot detection");
        }
        if(dragProxy == null)
        {
            dragProxy = new GameObject();
            dragProxy.transform.parent = transform;
            dragProxy.transform.localScale = new Vector3(1, 1, 1);
        }
        uiCanvas.alpha = 0;
    }

    void Update()
    {
        pingPong += Time.deltaTime * pingPongFreq;
        if(CanvasVisible && uiCanvas.alpha < 1)
        {
            uiCanvas.alpha += 1 * Time.deltaTime;
        }
        if(!CanvasVisible && uiCanvas.alpha > 0)
        {
            uiCanvas.alpha -= 1 * Time.deltaTime;
        }
    }

    public void ResetPingPong()
    {
        pingPong = 0;
    }

    /// <summary>
    /// Get/Set the currently selected PlayableEntity. If
    /// an entity is already selected, call its Deselect()
    /// method.
    /// </summary>
    public GameEntity Selected
    {
        get
        {
            return selected;
        }
        set
        {
            if (selected == value) return;
            if(selected != null)
            {
                ResetPingPong();
                selected.Deselect();
            }
            selected = value;
        }
    }

    /// <summary>
    /// Return the current mouse position over the gameboard.
    /// </summary>
    public Vector3 MousePosition
    {
        get
        {
            Collider col = gameBoardPlane.GetComponent<Collider>();
            Ray ray = Camera.main.ScreenPointToRay(Input.mousePosition);
            RaycastHit hit;
            if (col.Raycast(ray, out hit, RAY_LIMIT))
            {
                return new Vector3(hit.point.x, hit.point.y, hit.point.z);
            }
            else
            {
                return Vector3.zero;
            }
        }
    }

    /// <summary>
    /// Returns a value that oscillates between a min and max for UI effects (attached to the UIManager
    /// so all effects pulse in phase).
    /// </summary>
    public float PingPong
    {
        get
        {
            if (Selected != null) return 0;
            return Mathf.PingPong(pingPong, 0.75f) + 0.25f;
        }
    }

    /// <summary>
    /// Return the UnitSlot beneath the mouse, if any.
    /// </summary>
    public UnitSlot SlotTarget
    {
        get
        {
            Ray ray = Camera.main.ScreenPointToRay(Input.mousePosition);
            RaycastHit hit;
            UnitSlot slot = null;
            if (Physics.Raycast(ray, out hit, RAY_LIMIT, slotLayer))
            {
                slot = hit.collider.GetComponent<UnitSlot>();
                if(slot == null)
                {
                    UnitEntity entity = hit.collider.GetComponent<UnitEntity>();
                    if(entity != null)
                    {
                        slot = entity.Slot;
                    }
                }
                if(slot == null)
                {
                    Debug.LogWarning("Slot layer collision did not contain unit slot or unit entity");
                }
                return slot;
            }
            return null;
        }
    }

    public bool OverBoard
    {
        get
        {
            Ray ray = Camera.main.ScreenPointToRay(Input.mousePosition);
            RaycastHit hit;
            if (Physics.Raycast(ray, out hit, RAY_LIMIT, boardLayer))
            {
                return true;
            }
            return false;
        }
    }

    public void ShowHoverCard(GameEntity entity)
    {
        PrototypeData proto = GameManager.Instance.GetPrototype(entity.PrototypeId);
        if(proto != null)
        {
            hoverCard.Prototype = proto;
            hoverCard.Show(entity);
        }
    }

    public void HideHoverCard()
    {
        if(hoverCard.Visible)
        {
            hoverCard.Hide();
        }
    }

}
