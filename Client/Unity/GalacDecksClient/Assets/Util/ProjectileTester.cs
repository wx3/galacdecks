using UnityEngine;
using System.Collections.Generic;

/// <summary>
/// Convenient component for testing projectiles: shoots them from 
/// one object to another, repeatedly.
/// </summary>
public class ProjectileTester : MonoBehaviour {

    public List<GameObject> projectilePrefabs;
    public GameObject shooter;
    public GameObject target;
    public int minDamage, maxDamage;
    public int minShield, maxShield;
    public float delay;

    private int currentDamage;
    private float timer = 0;

    void Start()
    {
        currentDamage = minDamage;
    }

	void Update () {
        timer += Time.deltaTime;
        if(timer > delay)
        {
            DamageEffect damage = new DamageEffect();
            damage.damageTaken = currentDamage;
            damage.shieldBlocked = Random.Range(minShield, maxShield + 1);
            Fire(target, damage);
            timer = 0;
        }
	}

    protected void Fire(GameObject target, DamageEffect damage = null)
    {
        GameObject projectilePrefab = projectilePrefabs[0];
        if (damage != null)
        {
            if (damage.DamageTotal > 1)
            {
                if (damage.DamageTotal > projectilePrefabs.Count)
                {
                    projectilePrefab = projectilePrefabs[projectilePrefabs.Count - 1];
                }
                else
                {
                    projectilePrefab = projectilePrefabs[damage.DamageTotal - 1];
                }
            }
        }
        GameObject go = Instantiate(projectilePrefab, shooter.transform.position, Quaternion.identity) as GameObject;
        Projectile projectile = go.GetComponent<Projectile>();
        projectile.Target = target;
        projectile.Damage = damage;
        ++currentDamage;
        if(currentDamage > maxDamage)
        {
            currentDamage = minDamage;
        }
    }
}
