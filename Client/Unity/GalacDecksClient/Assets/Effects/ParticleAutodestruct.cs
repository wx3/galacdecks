using UnityEngine;
using System.Collections;

/**
 * Automatically destroys a gameobject with a particle system when the system
 * has emitted at least 1 particle but no longer has any particles.
 **/
public class ParticleAutodestruct : MonoBehaviour
{

    private ParticleSystem[] particleSystems;
    private bool started = false;

    void Start()
    {
        particleSystems = GetComponentsInChildren<ParticleSystem>();
        if (particleSystems.Length == 0)
        {
            Debug.LogWarning(this + " not attached to a particle system.");
            enabled = false;
        }
    }

    void Update()
    {
        if (!started)
        {
            foreach (ParticleSystem system in particleSystems)
            {
                if (system.particleCount > 0) started = true;
            }
        }
        if (started)
        {
            int count = 0;
            foreach (ParticleSystem system in particleSystems)
            {
                count += system.particleCount;
            }
            if (count == 0)
            {
                Destroy(gameObject);
            }
        }
    }
}
