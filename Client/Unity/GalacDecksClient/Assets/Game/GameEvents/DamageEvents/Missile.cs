using UnityEngine;
using System.Collections;

public class Missile : Projectile {

    public float turnRate;

    private float randomScale = 1;
    private GameObject hiddenAttractor;
    
    protected override void Start()
    {
        base.Start();
        hiddenAttractor = new GameObject();
        hiddenAttractor.name = "Hidden Attractor";
        float angle = Random.Range(0, Mathf.PI * 2);
        float dist = Vector3.Distance(transform.position, target.transform.position);
        float x = Mathf.Cos(angle) * dist;
        float z = Mathf.Sin(angle) * dist;
        Vector3 offset = Random.insideUnitSphere * dist * randomScale;
        hiddenAttractor.transform.position = new Vector3(target.transform.position.x + x, target.transform.position.y + 50, target.transform.position.z + z);
    }

    protected override void Update()
    {
        base.Update();
        if (finished)
        {
            Destroy(hiddenAttractor);
        }
        else
        {
            float step = speed * Time.deltaTime;
            hiddenAttractor.transform.position = Vector3.MoveTowards(hiddenAttractor.transform.position, target.transform.position, step * randomScale);
            transform.LookAt(hiddenAttractor.transform.position);
            GetComponent<Rigidbody>().velocity = transform.forward * speed;
        }
    }

    
}
