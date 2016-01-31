using UnityEngine;
using UnityEngine.UI;
using System.Collections;

/// <summary>
/// Locks a world space object's position to a canvas element, while keeping the same Y depth.
/// </summary>
public class WorldToCanvasLock : MonoBehaviour {

    public RectTransform targetTransform;
	
	// Update is called once per frame
	void Update () {
        Vector3 pos = Camera.main.ScreenToWorldPoint(targetTransform.transform.position);
        pos.y = transform.position.y;
        transform.position = pos;
    }
}
