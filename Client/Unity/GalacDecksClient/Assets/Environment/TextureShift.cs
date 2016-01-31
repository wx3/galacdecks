using UnityEngine;
using System.Collections;

/**
 * Shifts a texture offset over time.
 **/
public class TextureShift : MonoBehaviour
{
    public Color tint;
    public float shiftScale = 1;
    public Vector2 drift;
    public Vector2 apparentSpeed = Vector2.zero;


    private Vector2 offset;
    private Renderer _renderer;

    public Color Tint
    {
        get
        {
            return tint;
        }
        set
        {
            tint = value;
            _renderer = GetComponent<Renderer>();
            _renderer.material.SetColor("_TintColor", tint);
            _renderer.material.SetColor("_Color", tint);
        }
    }

    void Start()
    {
        Tint = tint;
    }

    void LateUpdate()
    {
        offset.x += (drift.x + apparentSpeed.x) * shiftScale * Time.deltaTime * 0.001f;
        offset.y += (drift.y + apparentSpeed.y) * shiftScale * Time.deltaTime * 0.001f;
        _renderer.material.SetTextureOffset("_MainTex", offset);
    }

}
