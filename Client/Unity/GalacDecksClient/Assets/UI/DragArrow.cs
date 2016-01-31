using UnityEngine;
using System.Collections;

public class DragArrow : MonoBehaviour {

    public Color defaultColor;
    public Vector3 origin;

    private Color color;

    private ParticleSystem[] particleSystems;
    private Renderer[] renderers;

    private bool isActive = true;

    public Color Color
    {
        get
        {
            return color;
        }
        set
        {
            if (color == value) return;
            color = value;
            foreach(ParticleSystem ps in particleSystems)
            {
                ps.startColor = color;
            }
            foreach(Renderer rend in renderers)
            {
                rend.material.SetColor("_TintColor", color);
            }
        }
    }

    public bool IsActive
    {
        get
        {
            return isActive;
        }
        set
        {
            if(isActive != value)
            {
                Cursor.visible = !value;
                isActive = value;
                foreach (ParticleSystem ps in particleSystems)
                {
                    ps.enableEmission = isActive;
                    if (!isActive)
                    {
                        ps.Clear();
                    }
                }
                foreach (Renderer rend in renderers)
                {
                    rend.enabled = isActive;
                }
            }
        }
    }

	void Start () {
        particleSystems = GetComponentsInChildren<ParticleSystem>();
        renderers = GetComponentsInChildren<Renderer>();
        Color = defaultColor;
        IsActive = false;
    }
	
	void Update () {
        Vector3 pos = UIManager.Instance.MousePosition + new Vector3(0,100,0);
        if(transform.position != pos)
        {
            transform.position = pos;
            Vector3 dir = origin - pos;
            float angle = Mathf.Atan2(dir.z, -dir.x) * 57.3f;
            transform.eulerAngles = new Vector3(0, angle, 0);
            //float dist = Vector3.Distance(pos, origin);
        }
        
    }
}
