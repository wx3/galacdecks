using UnityEngine;
using System.Collections.Generic;

/// <summary>
/// Handles the visual aspect of the Player's ship, such as engine glow while
/// loading.
/// </summary>
public class PlayerShipBehaviour : MonoBehaviour {

    public List<ParticleSystem> engines;
    public Color engineParticleColor;
    public float engineTransitionTime = 1;

    private bool enginesRunning = false;
    private float engineTimer = 999;

    public void StartEngines(float time)
    {
        enginesRunning = true;
        engineTimer = 0;
        engineTransitionTime = time;
    }

	public void CutEngines(float time)
    {
        enginesRunning = false;
        engineTimer = 0;
        engineTransitionTime = time;
    }

    void Update()
    {
        engineTimer += Time.deltaTime;
        Color engineColor = Color.clear;
        float amount = engineTimer / engineTransitionTime;
        if (enginesRunning)
        {
            engineColor = Color.Lerp(Color.clear, engineParticleColor, amount);
        } else
        {
            engineColor = Color.Lerp(engineParticleColor, Color.clear, amount);
        }
        foreach(ParticleSystem ps in engines)
        {
            ps.startColor = engineColor;
            if(engineColor == Color.clear)
            {
                if(ps.isPlaying)
                {
                    ps.Stop();
                }
                
            }
            else
            {
                if(!ps.isPlaying)
                {
                    ps.Play();
                }
            }
        }
    }
}
