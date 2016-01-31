using UnityEngine;
using System.Collections;

/// <summary>
/// The null target is the GameObject that detects whether a "no-target"
/// power is over the board.
/// </summary>
public class NullTarget : MonoBehaviour {

    public Color glowColor;

    private LerpTint lerpTint;

    void Awake()
    {
        lerpTint = GetComponent<LerpTint>();
    }

	// Use this for initialization
	void Start () {
	
	}
	
	// Update is called once per frame
	void Update () {
	
	}
}
