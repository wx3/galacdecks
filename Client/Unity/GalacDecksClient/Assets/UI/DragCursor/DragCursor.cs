using UnityEngine;
using System.Collections.Generic;

public class DragCursor : MonoBehaviour {

    public GameObject dashParticle;
    public GameObject pointer;
    public float dashFreq;
    public float speed;
    public float midPointHeight;
    public float pointerHeight;

    public Color defaultColor;
    public Color validAttackColor;


    private float length;
    private LineRenderer line;
    private List<DragDash> dashes = new List<DragDash>();
    private Vector3 dragStart;
    private float timer;
    private bool dragging;
    private Color color;

    float CursorPlane
    {
        get
        {
            return 20;
        }
    }

    public float Length
    {
        get
        {
            return length;
        }
    }

    public Vector3 Position
    {
        get
        {
            return UIManager.Instance.MousePosition + (Vector3.up * pointerHeight);
        }
    }

    public Vector3 Origin
    {
        get
        {
            return dragStart;
        }
    }

    public Color Color
    {
        get
        {
            return color;
        }
        set
        {
            color = value;
            Color startColor = Color.Lerp(Color.clear, color, 0.5f);
            line.SetColors(startColor, color);
            pointer.GetComponent<LerpTint>().SetColor(color, 0);
        }
    }

	// Use this for initialization
	void Start () {
        line = GetComponent<LineRenderer>();
        if (dashFreq <= 0) dashFreq = 1;
	}
	
	// Update is called once per frame
	void Update () {
        timer += Time.deltaTime;
        if(dragging)
        {
            line.enabled = true;
            pointer.SetActive(true);
            if (timer >= 1 / dashFreq)
            {
                //SpawnDash();
                timer = 0;
            }
            UpdateColor();
            UpdateLine();
            //UpdateCursor();
            UpdateDashes();
            length = Vector3.Distance(Position, dragStart);
        }
        else
        {
            line.enabled = false;
            pointer.SetActive(false);
        }
	}

    public void StartDrag(Vector3 position)
    {
        Cursor.visible = false;
        dragStart = position;
        dragging = true;
    }

    public void EndDrag()
    {
        Cursor.visible = true;
        dragging = false;
    }

    private void UpdateColor()
    {
        if(UIManager.Instance.Selected is UnitEntity)
        {
            UnitEntity unit = (UnitEntity)UIManager.Instance.Selected;
            UnitSlot slot = UIManager.Instance.SlotTarget;
            if(slot != null && CommandManager.Instance.IsValidAttack(unit, slot.Unit))
            {
                Color = validAttackColor;
            }  else
            {
                Color = defaultColor;
            }
        }
    }

    private void UpdateLine()
    {
        Vector3 midPoint = Vector3.Lerp(dragStart, Position, 0.5f);
        midPoint.y += midPointHeight;
        int segments = 10;
        line.SetVertexCount(segments + 1);
        Vector3 p = dragStart;
        Vector3 dir = Vector3.zero;
        for(int i = 0; i <= segments; i++)
        {
            float t = (float) i / (float) segments;
            Vector3 newP = Bezier.CalculatePoint(dragStart, midPoint, midPoint, Position, t);
            dir = newP - p;
            p = newP;
            line.SetPosition(i, p);
        }
        transform.LookAt(p + dir);
        transform.position = Position;
    }

    private void SpawnDash()
    {
        GameObject go = (GameObject)Instantiate(dashParticle, dragStart, Quaternion.identity);
        go.transform.eulerAngles = new Vector3(90, 0, 0);
        DragDash dash = go.GetComponent<DragDash>();
        dash.cursor = this;
        dash.speed = this.speed;
        dashes.Add(dash);
    }

    private void UpdateDashes()
    {

    }
    
    private void UpdateSegment(GameObject segment)
    {
        Vector3 cursorPos = Position;
        // Direction from drag start to where cursor is now:
        Vector3 dir = (cursorPos - dragStart).normalized;
        // How far is the segment from current pos:
        float currentDist = Vector3.Distance(cursorPos, segment.transform.position);
        float newDist = currentDist - (speed * Time.deltaTime);
        segment.transform.position = cursorPos - (dir * newDist);
        float angle = Mathf.Atan2(dir.z, dir.x) * 57;
        segment.transform.eulerAngles = new Vector3(segment.transform.eulerAngles.x, angle - 90, segment.transform.eulerAngles.z);
    }
}
