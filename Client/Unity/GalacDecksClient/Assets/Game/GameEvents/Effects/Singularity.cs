using UnityEngine;
using System.Collections;

public class Singularity : ClientPrefabBehaviour {

    public float duration;

    private float peakBrightness = 1;
    private ParticleSystem[] particleSystems;
    private Light light;

    void Awake()
    {
        particleSystems = GetComponentsInChildren<ParticleSystem>();
        light = GetComponent<Light>();
        peakBrightness = light.intensity;
        light.intensity = 0;
    }

    protected override void Update()
    {
        base.Update();
        if(elapsed < duration)
        {
            if(light.intensity < peakBrightness)
            {
                light.intensity += Time.deltaTime * 4;
            }
        }
        else {
            if(light.intensity > 0)
            {
                light.intensity -= Time.deltaTime * 4;
            }
            foreach(ParticleSystem ps in particleSystems)
            {
                ps.emissionRate = ps.emissionRate * 0.95f;
            }
        }
    }
}
