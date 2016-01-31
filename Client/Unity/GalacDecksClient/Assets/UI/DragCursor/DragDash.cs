using UnityEngine;
using System.Collections;

public class DragDash : MonoBehaviour {

    public DragCursor cursor;
    public LerpTint tint;
    public Color color;
    public float speed;

    private float lastDist;

    void Awake()
    {
        tint = GetComponent<LerpTint>();
    }

	// Use this for initialization
	void Start () {
        lastDist = Vector3.Distance(transform.position, cursor.Position);
    }
	
	// Update is called once per frame
	void Update () {
        Vector3 dir = (cursor.Origin - cursor.Position).normalized;
        float newDist = lastDist - (speed * Time.deltaTime);
        transform.position = cursor.Position + (dir * newDist);
        lastDist = Vector3.Distance(transform.position, cursor.Position);

        float angle = Mathf.Atan2(dir.z, -dir.x) * 57.3f;
        transform.eulerAngles = new Vector3(90, angle + 90, 0);

        if(newDist < 0 || newDist > cursor.Length)
        {
            Destroy(gameObject);
        }
    }
}
