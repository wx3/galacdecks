using UnityEngine;
using System.Collections;

/// <summary>
/// Tutorial for the EndTurn choices
/// </summary>
public class EndTurnChoice : TutorialScript {

    public bool allowEnergy = true;
    public bool allowCommand = true;
    public bool allowDraw = true;

    private EndTurnChoiceDialog endTurnDialog;

    public override bool IsTriggered
    {
        get
        {
            if (!base.IsTriggered) return false;
            if (endTurnDialog != null) return true;
            return false;
        }
    }

    public override void HandleCommand(GameCommand command)
    {
        base.HandleCommand(command);
        if (command is EndTurnCommand) IsSatisfied = true;
    }

    void Update()
    {
        if(endTurnDialog == null)
        {
            endTurnDialog = GameObject.FindObjectOfType<EndTurnChoiceDialog>();
            if(endTurnDialog != null)
            {
                endTurnDialog.energyChoice.interactable = allowEnergy;
                endTurnDialog.mineralChoice.interactable = allowCommand;
                endTurnDialog.drawChoice.interactable = allowDraw;
            }
        }
    }
}
