using UnityEngine;
using System.Collections;
using System;

public class ClientPrefabBehaviour : GameEventBehaviour {

    private ClientPrefabEvent data;

    public GameEntity target;
    public bool centerOnTarget;

    public override GameEvent Data
    {
        set
        {
            data = (ClientPrefabEvent)value;
        }
    }

    void Start()
    {
        if(data != null)
        {
            target = GameManager.Instance.GetEntity(data.entityId);
            if (centerOnTarget)
            {
                transform.position = target.transform.position;
            }
        }
    }    
	
}
