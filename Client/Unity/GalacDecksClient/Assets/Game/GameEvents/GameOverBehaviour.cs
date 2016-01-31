using UnityEngine;
using System.Collections;
using System;

public class GameOverBehaviour : GameEventBehaviour {

    public GameObject gameOverDialog;
    private GameOverEvent data;

    public override GameEvent Data
    {
        set
        {
            this.data = (GameOverEvent)value;
        }
    }

    // Use this for initialization
    void Start () {
        Debug.Log("Game over");
        GameManager.Instance.gameOverHandler();
	}

}
