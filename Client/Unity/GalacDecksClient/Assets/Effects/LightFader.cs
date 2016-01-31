using UnityEngine;
using System.Collections;

/// <summary>
/// Adjusts a light's intensity by a curve.
/// </summary>
public class LightFader : MonoBehaviour {

    public float duration;
    public AnimationCurve curve;

    private float normalIntensity;
    private float normalFlare;
    private float timer;
    private Light _light;
    private ProFlare flare;

	void Start () {
        _light = GetComponent<Light>();
        if(_light == null)
        {
            Debug.LogWarning("Expected light component");
            enabled = false;
            return;
        }
        normalIntensity = _light.intensity;
        flare = GetComponentInChildren<ProFlare>();
        if(flare != null)
        {
            normalFlare = flare.GlobalScale;
        }
    }
	
	void Update () {
        timer += Time.deltaTime;
        float amount = 1;
        if(duration > 0)
        {
            amount = curve.Evaluate(timer / duration);
        }
        _light.intensity = amount * normalIntensity;
        if(flare != null)
        {
            flare.GlobalScale = normalFlare * amount;
        }
	}
}
