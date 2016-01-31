using UnityEngine;
using System.Collections;

/// <summary>
/// Cause a gameobject to orbit another at a fixed period in the local Y plane.
/// </summary>
public class Orbit : MonoBehaviour {

    public GameObject target;
    public float period;

    private float dist;
    private float elapsed;
    private float angle;

	void Start () {
        if (target == null)
        {
            enabled = false;
            return;
        }
        Vector3 pos = transform.localPosition - target.transform.localPosition;
        angle = Mathf.Atan2(pos.z, pos.x);
        dist = Vector3.Distance(transform.localPosition, target.transform.localPosition);
        if (period == 0f) period = 1;
	}
	
	void Update () {
        angle += (2 * Mathf.PI / period) * Time.deltaTime;
        float x = Mathf.Cos(angle) * dist;
        float z = Mathf.Sin(angle) * dist;
        transform.localPosition = new Vector3(target.transform.localPosition.x + x, transform.localPosition.y, target.transform.localPosition.z + z);
	}
}
