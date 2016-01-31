using UnityEngine;
using System.Collections;

public class Star : MonoBehaviour {

    public float twinkleProbability;

    private Renderer _renderer;

	// Use this for initialization
	void Start () {
        _renderer = GetComponentInChildren<Renderer>();
	}
	
	// Update is called once per frame
	void Update () {
	    if(Random.Range(0f,1f) < twinkleProbability)
        {
            _renderer.enabled = false;
        }
         else
        {
            _renderer.enabled = true;
        }
	}
}
