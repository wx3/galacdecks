using UnityEngine;
using System.Collections;

/// <summary>
/// Produces the effect indicating this unit is currently disabled.
/// </summary>
public class Disabled : MonoBehaviour {

    private UnitEntity unit;
    private bool effectActive = false;
    private ParticleSystem[] particleSystems;

	
	void Start () {
        particleSystems = GetComponentsInChildren<ParticleSystem>();
        unit = GetComponentInParent<UnitEntity>();
        if(unit == null)
        {
            Debug.LogWarning("Disabled expected to be under UnitEntity");
            enabled = false;
        }
        foreach (ParticleSystem ps in particleSystems)
        {
            var em = ps.emission;
            em.enabled = false;
        }
    }
	
	
	void Update () {
	    if(unit.EntityView.GetStat("DISABLED") > 0)
        {
            if(!effectActive)
            {
                foreach(ParticleSystem ps in particleSystems)
                {
                    var em = ps.emission;
                    em.enabled = true;
                    Debug.Log("Enabling " + ps);
                }
                effectActive = true;
            }
        } else
        {
            if(effectActive)
            {
                foreach (ParticleSystem ps in particleSystems)
                {
                    var em = ps.emission;
                    em.enabled = false;
                }
                effectActive = false;
            }
        }
	}
}
