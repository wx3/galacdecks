using UnityEngine;
using System.Collections.Generic;

public class DamageCausedEvent : GameEvent {

    public int attackerId;
    public List<DamageEffect> damages;

}
