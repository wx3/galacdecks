using UnityEngine;
using System.Collections;

[RequireComponent(typeof(Camera))]
public class StarSystemsCamera : MonoBehaviour {

    public float zoomTime;
    public float peakFlare;
    public AnimationCurve zoomCurve;

    private Vector3 startPosition;
    private Camera _camera;
    private float elapsed;
    private StarSystem targetSystem;

    public StarSystem TargetSystem
    {
        get
        {
            return targetSystem;
        }
        set
        {
            if(targetSystem != value)
            {
                elapsed = 0;
                targetSystem = value;
            }
        }
    }

	void Start () {
        _camera = GetComponent<Camera>();
        startPosition = _camera.transform.position;
    }
	
	void Update () {
        if(targetSystem != null)
        {
            elapsed += Time.deltaTime;
            float amount = zoomCurve.Evaluate(elapsed / zoomTime);
            Vector3 pos = Vector3.Lerp(startPosition, targetSystem.sun.transform.position, amount);
            _camera.transform.position = pos;
        }
        
	}
}
