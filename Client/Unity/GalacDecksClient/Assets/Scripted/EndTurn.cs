using UnityEngine;
using System.Collections;

public class EndTurn : TutorialScript {

    void Update()
    {
        if (GameObject.FindObjectOfType<EndTurnChoiceDialog>() != null)
        {
            IsSatisfied = true;
        }
    }
}
