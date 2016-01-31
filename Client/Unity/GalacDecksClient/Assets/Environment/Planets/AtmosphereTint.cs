using UnityEngine;
using System.Collections;

public class AtmosphereTint : MonoBehaviour
{

    public Color color;

    Material sharedMaterial;
    Renderer _renderer;

    void Start()
    {
        _renderer = GetComponent<Renderer>();
    }

    // Update is called once per frame
    void Update()
    {
        if (sharedMaterial == null)
        {
            sharedMaterial = new Material(_renderer.sharedMaterial);
        }
        sharedMaterial.SetColor("_TintColor", color);
        _renderer.sharedMaterial = sharedMaterial;
    }
}
