using UnityEngine;
using System.Collections;
using System;

public class EndTurnBehaviour : GameEventBehaviour {

    private EndTurnEvent data;

    public override GameEvent Data
    {
        set
        {
            data = (EndTurnEvent)value;
        }
    }
	
	override protected void Update () {
        base.Update();
        GameManager.Instance.Turn = data.newTurn;
        Destroy(gameObject);
	}
}
