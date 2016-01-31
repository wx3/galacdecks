using UnityEngine;
using System.Collections;

[RequireComponent(typeof(Rigidbody))]
[RequireComponent(typeof(Collider))]
public class Projectile : MonoBehaviour {

    public float speed;
    public GameObject impactEffect;
    public GameObject damageNumberPrefab;
    public float maxLifetime = 1;

    protected DamageEffect damage;
    protected bool finished;
    protected GameObject target;
    protected ParticleSystem[] particleSystems = new ParticleSystem[0];
    protected Renderer[] renderers;
    protected Light _light;
    protected ProFlare flare;

    protected float timer = 0;

    public GameObject Target
    {
        get
        {
            return target;
        }
        set
        {
            target = value;
        }
    }

    public virtual DamageEffect Damage
    {
        get
        {
            return damage;
        }
        set
        {
            damage = value;
        }
    }


    public bool Finished
    {
        get
        {
            return finished;
        }
    }

    /// <summary>
    /// The default behaviour of a projectile is to head towards it's target at speed:
    /// </summary>
    protected virtual void Start()
    {
        if(target != null)
        {
            transform.LookAt(target.transform.position);
        }
        GetComponent<Rigidbody>().velocity = transform.forward * speed;
        particleSystems = GetComponentsInChildren<ParticleSystem>();
        renderers = GetComponentsInChildren<MeshRenderer>();
        _light = GetComponentInChildren<Light>();
        flare = GetComponentInChildren<ProFlare>();
        timer = 0;
    }

    protected virtual void Update()
    {
        timer += Time.deltaTime;
        if (target == null)
        {
            finished = true;
        }
        if(finished)
        {
            if(_light != null)
            {
                _light.intensity = _light.intensity * 0.93f;
            }
            if(flare != null)
            {
                flare.GlobalScale = flare.GlobalBrightness * 0.93f;
            }
        }
        if(timer > maxLifetime)
        {
            Destroy(gameObject);
        }
    }

    void OnTriggerEnter(Collider other)
    {
        if(other.gameObject == target && !finished)
        {
            Impact();
        }
        Forcefield field = other.GetComponent<Forcefield>();
        if (field != null)
        {
            Debug.Log("Hit field");
            field.OnHit(transform.position, -2, 1);
        }
    }

    protected void Impact()
    {
        GetComponent<Rigidbody>().velocity = Vector3.zero;
        if (impactEffect != null)
        {
            GameObject impact = Instantiate(impactEffect);
            impact.transform.position = new Vector3(transform.position.x, transform.position.y + 5, transform.position.z);
        }
        if(damage != null)
        {
            UnitEntity unit = target.GetComponent<UnitEntity>();
            if(unit != null)
            {
                unit.dealDamage(damage);
            }
        }
        foreach (ParticleSystem system in particleSystems)
        {
            system.Stop();
        }
        foreach (Renderer renderer in renderers)
        {
            renderer.enabled = false;
            Debug.Log("disabling " + renderer);
        }
        finished = true;
    }
}
