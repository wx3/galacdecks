using UnityEngine;
using System.Collections;

public class Sweep : MonoBehaviour {

    public float speed;

    private Vector3 movement;
    private ParticleSystem[] particleSystems;

    // Use this for initialization
    void Start () {
        transform.position = new Vector3(-150, 0, 0);
        movement = new Vector3(speed, 0, 0);
        particleSystems = GetComponentsInChildren<ParticleSystem>();
    }
	
	// Update is called once per frame
	void Update () {
        transform.position += movement * Time.deltaTime;
        if(transform.position.z > 250)
        {
            foreach (ParticleSystem system in particleSystems)
            {
                system.enableEmission = false;
            }
        }
        
    }
}
