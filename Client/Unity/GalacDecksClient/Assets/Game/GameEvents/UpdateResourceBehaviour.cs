using UnityEngine;
using System.Collections;
using System;

public class UpdateResourceBehaviour : GameEventBehaviour {

    private UpdateResourceEvent data;

    public override GameEvent Data
    {
        set
        {
            data = (UpdateResourceEvent)value;
        }
    }

    void Start () {
        if(data.playerName == GameManager.Instance.MyPlayer.playerName)
        {
            if(data.resource == "ENERGY_RESOURCE")
            {
                UIManager.Instance.energyStats.Value = data.newValue;
            }
            else if(data.resource == "MINERAL_RESOURCE")
            {
                UIManager.Instance.mineralStats.Value = data.newValue;
            }
            else {
                Debug.LogWarning("Invalid resource type: " + data.resource);
            }
           
        }
        else
        {

        }
    }
	
}
