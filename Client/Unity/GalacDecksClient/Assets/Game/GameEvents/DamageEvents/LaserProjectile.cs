using UnityEngine;
using System.Collections;

public class LaserProjectile : Projectile {

    private float damageScale = 1;
    private Vector3 origin;
    private float elapsed;

    public override DamageEffect Damage
    {
        set
        {
            base.Damage = value;
            damageScale = value.damageTaken + value.shieldBlocked;
            if (damageScale > 10) damageScale = 10;
        }
    }

    protected override void Start()
    {
        base.Start();
        origin = transform.position;
        foreach(ParticleSystem ps in particleSystems)
        {
            float normalEmission = ps.emissionRate;
            ps.emissionRate = (normalEmission / 4) + (normalEmission * damageScale / 4);
            
        }
    }

    protected override void Update()
    {
        base.Update();
        elapsed += Time.deltaTime;
    }
}
