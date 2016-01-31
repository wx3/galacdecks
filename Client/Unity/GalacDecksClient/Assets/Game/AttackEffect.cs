using UnityEngine;
using System.Collections;

public class AttackEffect : MonoBehaviour {

    public GameObject projectilePrefab;
    public int shots;
    public float delay;
    public float projectileSpeed = 10;

    private float delayTimer;
    private int shotsFired;
    private GameObject attacker;
    private GameObject target;

    public GameObject Attacker
    {
        get
        {
            return attacker;
        }
        set
        {
            attacker = value;
        }
    }

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

	// Use this for initialization
	void Start () {
        delayTimer = delay;
	}
	
	// Update is called once per frame
	void Update () {
	    if(shotsFired < shots)
        {
            if(delayTimer >= delay)
            {
                Fire();
            } else
            {
                delayTimer += Time.deltaTime;
            }
        } 
        else
        {
            Destroy(gameObject);
        }
	}

    protected void Fire()
    {
        if (attacker == null || target == null) return;
        delayTimer = 0;
        Vector3 pos = attacker.transform.position;
        //pos.y = 100;
        GameObject go = Instantiate(projectilePrefab, pos, Quaternion.identity) as GameObject;
        Vector3 targetPos = target.transform.position;
        //targetPos.y = 100;
        go.transform.LookAt(targetPos);
        go.GetComponent<Projectile>().Target = target;
        ++shotsFired;
        Debug.Log("fired");
    }
}
