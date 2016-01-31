using UnityEngine;
using System.Collections;

public class Shockwave : MonoBehaviour {

    public float growFactor;

    private float scale;

	// Use this for initialization
	void Start () {
        scale = transform.localScale.x;
	}
	
	// Update is called once per frame
	void Update () {
        scale += growFactor * Time.deltaTime;
        transform.localScale = new Vector3(scale, scale, scale);
	}
}
