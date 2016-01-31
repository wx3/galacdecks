using UnityEngine;
using System.Collections;

/// <summary>
/// Behaviour for simple damage that displays the damage counter.
/// </summary>
public class SimpleDamageBehaviour : BaseDamageEventBehaviour {

    public GameObject damageCounterPrefab;

	void Start () {
        for(int i = 0; i < data.damages.Count; i++)
        {
            DamageEffect damage = data.damages[i];
            GameEntity target = targets[i];
            UnitEntity unit = target.GetComponent<UnitEntity>();
            unit.dealDamage(damage);
        }
        
	}
	
}
