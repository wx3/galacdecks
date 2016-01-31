using UnityEngine;
using System.Collections;

public class ResourceFly : MonoBehaviour {

    public float startSpeed;
    public float maxSpeed;
    public float minDistance;
    public float accelTime = 1;
    public GameObject visual;

    public GameObject flashPrefab;

    public bool done;
    private Transform attractor;
    private Rigidbody _rigidbody;
    private Vector3 startVelocity;
    private ParticleSystem[] particleSystems;
    private float elapsed;

    public Transform Attractor
    {
        set
        {
            attractor = value;
        }
    }
    
    public bool Done
    {
        get
        {
            return done;
        }
    }

    void Awake()
    {
        _rigidbody = GetComponent<Rigidbody>();
        particleSystems = GetComponentsInChildren<ParticleSystem>();
    }

	void Start () {
        
        Vector3 startLook = new Vector3(transform.position.x + Random.Range(-10, 10), transform.position.y + 20, transform.position.z + 30);
        transform.LookAt(startLook);
        startVelocity = transform.forward * startSpeed;
        _rigidbody.velocity = startVelocity;
        
    }

    void FixedUpdate()
    {
        if(attractor == null)
        {
            Destroy(gameObject);
        }
        if (!done && attractor != null)
        {
            elapsed += Time.deltaTime;
            float dist = Vector3.Distance(attractor.position, transform.position);
            if (dist < minDistance)
            {
                Vector3 pos = new Vector3(attractor.position.x, attractor.position.y + 1, attractor.position.z);
                Instantiate(flashPrefab, pos, Quaternion.identity);
                done = true;
                visual.SetActive(false);
                _rigidbody.velocity = Vector3.zero;
                foreach (ParticleSystem ps in particleSystems)
                {
                    ps.enableEmission = false;
                }
            }
            else
            {
                Vector3 direction = (attractor.position - transform.position).normalized;
                _rigidbody.velocity = Vector3.Lerp(startVelocity, direction * maxSpeed, elapsed / accelTime);
            }
        }
    }
}
