using UnityEngine;
using System.Collections.Generic;

public class Gameboard : MonoBehaviour {

    static string GetIndex(string playerName, int row, int col)
    {
        return playerName + "_" + row + "x" + col;
    }

    public GameObject spaceSlotPrototype;
    public bool showCoordinates;

    public float dividerGap;
    public float horizontalSpacing;
    public float verticalSpacing;
    
    public Transform spaceSlots;

    public Transform cardReveal;

    /// <summary>
    /// What color does the board glow when a target-less card is over it?
    /// </summary>
    public Color glowColor;

    public LerpTint boardTint;

    private List<EntityCoordinates> coordinates;
    private UnitSlot[,] unitSlots;


    void Start()
    {
        boardTint.SetColor(Color.clear, 0);
    }

    void Update()
    {
        bool glow = false;

        if(UIManager.Instance.OverBoard)
        {
            if (UIManager.Instance.Selected)
            {
                if(CommandManager.Instance.IsValidPlay(UIManager.Instance.Selected, null)) {
                    glow = true;
                }
            }
        }
        if(glow)
        {
            boardTint.SetColor(glowColor, 0.5f);
        }
        else
        {
            boardTint.SetColor(Color.clear, 0.5f);
        }
    }


    /// <summary>
    /// Initialize the board layout from a GameView. It's important to remember that 
    /// the board appears rotated 180 degrees from player 2's perspective, but all 
    /// the slots still face the active player.
    /// </summary>
    /// <param name="gameView"></param>
    public void InitializeFromView(GameView gameView)
    {
        coordinates = gameView.coordinates;
        // First, figure out the maximum dimensions of the board:
        int maxX = 0, maxY = 0;
        foreach(EntityCoordinates coord in gameView.coordinates)
        {
            if (coord.x > maxX) maxX = coord.x;
            if (coord.y > maxY) maxY = coord.y;
        }
        int columns = maxX + 1;
        int rows = maxY + 1;
        unitSlots = new UnitSlot[columns, rows];
        bool reverseView = false;
        if (gameView.viewerPosition == 2) reverseView = true;
        Vector2 center = new Vector2((float) (columns - 1) / 2, (float) (rows - 1) / 2);
        Debug.Log(center);
        foreach(EntityCoordinates coord in gameView.coordinates)
        {
            if (unitSlots[coord.x, coord.y] == null)
            {
                Vector3 pos = GetSlotPosition(coord, center, reverseView);
                UnitSlot slot = CreateSlot(pos, spaceSlotPrototype, spaceSlots);
                slot.x = coord.x;
                slot.y = coord.y;
                unitSlots[coord.x, coord.y] = slot;
            }
        }
        InitializeEntities(gameView);
    }

    public UnitSlot GetSlot(int column, int row)
    {
        return unitSlots[column, row];
    }

    public UnitSlot GetSlot(EntityCoordinates coord)
    {
        return unitSlots[coord.x, coord.y];
    }

    public void PlaceUnit(UnitEntity unit)
    {
        int row = unit.EntityView.row;
        int col = unit.EntityView.column;
        UnitSlot slot = unitSlots[col,row];
        if(slot == null)
        {
            throw new System.Exception("Missing slot: " + col + "x" + row);
        }
        if(slot.Unit != null)
        {
            Debug.LogError("Slot already has a unit, that shouldn't happen");
        }
        slot.Unit = unit;
        unit.transform.localPosition = Vector3.zero;
        unit.transform.localRotation = Quaternion.identity;
    }

   
    /// <summary>
    /// Return the slot position relative to a particular coordinates as "center".
    /// and perspective.
    /// </summary>
    /// <param name="coord"></param>
    /// <param name="reverseCoordinates"></param>
    /// <returns></returns>
    private Vector3 GetSlotPosition(EntityCoordinates coord, Vector2 center, bool reverseCoordinates)
    {
        Vector3 pos = Vector3.zero;
        float xDiff = coord.x - center.x;
        float yDiff = center.y - coord.y;
        pos.x = (xDiff * horizontalSpacing);// + (horizontalSpacing / 2);

        pos.z = (yDiff * verticalSpacing) - (verticalSpacing / 2);
        if (reverseCoordinates)
        {
            pos.x = -pos.x;
            pos.z = -pos.z;
            if (coord.x % 2 == 0)
            {
                pos.z -= verticalSpacing / 2;
            }
        }
        else
        {
            if (coord.x % 2 == 0)
            {
                pos.z += verticalSpacing / 2;
            }
        }
        return pos;
    }
    
    private UnitSlot CreateSlot(Vector3 position, GameObject prefab, Transform container)
    {
        GameObject go = GameObject.Instantiate(prefab);
        UnitSlot slot = go.GetComponentInChildren<UnitSlot>();
        go.transform.SetParent(container);
        go.transform.localRotation = Quaternion.identity;
        go.transform.localPosition = new Vector3(position.x, 0, position.z);
        return slot;
    }


    private void InitializeEntities(GameView gameView)
    {
        foreach(EntityView ev in gameView.inPlay)
        {
            if(ev.OnBoard)
            {
                UnitEntity unit = GameManager.Instance.SpawnUnit(ev);
                PlaceUnit(unit);
            }
        }
    }

}
