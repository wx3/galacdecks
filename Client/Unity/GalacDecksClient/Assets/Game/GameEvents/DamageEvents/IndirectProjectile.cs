using UnityEngine;
using System.Collections;

/// <summary>
/// A projectile that moves along an arc toward the target.
/// </summary>
public class IndirectProjectile : Projectile {

    // How high does the projectile go as a multiple of the straight distance?
    public float heightScale;

    private Vector3 start;
    private Vector3 midPoint;
    private float height;
    private float fixedTimer;
    private float duration;

    protected override void Start()
    {
        base.Start();
        float dist = Vector3.Distance(start, target.transform.position);
        float height = heightScale * dist;
        start = transform.position;
        midPoint = Vector3.Lerp(start, target.transform.position, 0.5f);
        midPoint.y += height;
        fixedTimer = 0;
        duration = Bezier.Length(start, midPoint, midPoint, target.transform.position, 10) / speed;
        GetComponent<Rigidbody>().velocity = Vector3.zero;
    }

    protected override void Update()
    {
        base.Update();
    }

    void FixedUpdate()
    {
        if (!finished && target != null)
        {
            fixedTimer += Time.deltaTime;
            float t = Mathf.Clamp(fixedTimer / duration, 0, 1);
            Vector3 pos = Bezier.CalculatePoint(start, midPoint, midPoint, target.transform.position, t);
            transform.position = pos;
            Vector3 next = Bezier.CalculatePoint(start, midPoint, midPoint, target.transform.position, t + 0.01f);
            transform.LookAt(next);
        }
    }
}
