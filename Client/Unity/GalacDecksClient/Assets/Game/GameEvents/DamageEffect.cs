using UnityEngine;
using System.Collections;

public class DamageEffect {

    public int entityId;
    public int damageTaken;
    public int shieldBlocked;

    /// <summary>
    /// This is the "payload" of the damgae-- if using different projectile effects
    /// based on damage, this is how much damage the projectile would have delivered
    /// with no reduction by shields.
    /// </summary>
    public int DamageTotal
    {
        get
        {
            return damageTaken + shieldBlocked;
        }
    }
}
