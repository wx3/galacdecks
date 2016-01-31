using UnityEngine;
using UnityEngine.UI;
using System.Collections.Generic;

/// <summary>
/// Convenient component for gradually changing the tint of a material or UI image.
/// </summary>
public class LerpTint : MonoBehaviour {

    private Image image;
    private Renderer _renderer;
    private string shaderColor = "_Color";
    private Color startColor;
    private Color targetColor;
    private float timer = 0;
    private float duration;

    private Color Color
    {
        get
        {
            if (image != null) return image.color;
            return _renderer.GetComponent<Renderer>().material.GetColor(shaderColor);
        }
        set
        {
            if (image != null)
            {
                image.color = value;
                return;
            }
            foreach(Material mat in _renderer.materials)
            {
                mat.SetColor(shaderColor, value);
            }
        }
    }

    public void SetColor(Color newColor, float duration = 0)
    {
        // Don't do anything if we're being set to the same color:
        if(!newColor.Equals(targetColor))
        {
            startColor = Color;
            targetColor = newColor;
            this.duration = duration;
            timer = 0;
        } 
    }

    /// <summary>
    /// Do init during awake because other objects may call on us during their Start()
    /// </summary>
    void Awake () {
        _renderer = GetComponent<Renderer>();
        image = GetComponent<Image>();
        if(image != null)
        {
            // Don't do anything
        }
        else if(_renderer.material.HasProperty("_TintColor"))
        {
            shaderColor = "_TintColor";
        }
        else if(_renderer.material.HasProperty("_Color"))
        {
            shaderColor = "_Color";
        } 
        else
        {
            Debug.LogWarning("Couldn't identify image or material shader color");
            enabled = false;
            return;
        }
        startColor = Color;
        targetColor = Color;
	}
	
	void Update () {
        if (Color.Equals(targetColor)) return;
        float amount = 1;
        if(duration > 0)
        {
            amount = timer / duration;
        }
        Color = Color.Lerp(startColor, targetColor, amount);
        timer += Time.deltaTime;
    }
}
