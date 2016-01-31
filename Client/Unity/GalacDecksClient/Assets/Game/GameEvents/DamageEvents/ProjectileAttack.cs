using UnityEngine;
using System.Collections.Generic;

/// <summary>
/// Damage behaviour that fires one projectile per target.
/// </summary>
public class ProjectileAttack : BaseDamageEventBehaviour
{
    /// <summary>
    /// List of projectile damages, by damage. The first projectile prefab
    /// handles damage <=1, the next <=2, etc.
    /// </summary>
    public List<GameObject> projectilePrefabs;
    public float shotDelay;

    private float shotTimer;
    private int shotCounter = 0;
    // Which target index in targets are we shooting at?
    private int currentTarget = 0;
    private List<Projectile> projectiles = new List<Projectile>();

    /// <summary>
    /// A projectile attack blocks until all projectiles have fired and finished.
    /// </summary>
    public override bool IsBlocking
    {
        get
        {
            if (currentTarget < targets.Count) return true;
            foreach(Projectile proj in projectiles)
            {
                if (!proj.Finished) return true;
            }
            return base.IsBlocking;
        }
    }

    void Start()
    {
        if(cause == null)
        {
            Remove();
            return;
        }
    }

    override protected void Update()
    {
        base.Update();
        if(cause == null)
        {
            Remove();
            return;
        }

        if(currentTarget < targets.Count)
        {
            DamageEffect dmg = data.damages[currentTarget];
            if(shotTimer > shotDelay)
            {
                GameEntity target = targets[currentTarget];
                Fire(target.gameObject, dmg);
                shotTimer = 0;
                ++shotCounter;
                ++currentTarget;
            }
            else
            {
                shotTimer += Time.deltaTime;
            }
        }
    }

    protected void Fire(GameObject target, DamageEffect damage = null)
    {
        GameObject projectilePrefab = projectilePrefabs[0];
        if(damage != null)
        {
            if(damage.DamageTotal > 1)
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
        Vector3 pos = cause.transform.position;
        pos.y += 15;
        GameObject go = Instantiate(projectilePrefab, pos, Quaternion.identity) as GameObject;
        Projectile projectile = go.GetComponent<Projectile>();
        projectile.Target = target;
        projectile.Damage = damage;
        projectiles.Add(projectile);
    }

    protected override void Remove()
    {
        base.Remove();
    }

}
