using UnityEngine;
using System.Collections;
using System;

public class AttackCommandBehaviour : CommandBehaviour {

    public Color crossHairColor;
    public LerpTint crosshairs;
    public LerpTransform lerperTransform;

    private AttackCommand command;
    private UnitEntity attacker;
    private UnitEntity target;
    private UnitSlot targetSlot;


    public override GameCommand Command
    {
        get
        {
            return command;
        }
        set
        {
            command = (AttackCommand)value;
        }
    }

    // Use this for initialization
    void Start () {
        targetSlot = GameManager.Instance.gameBoard.GetSlot(command.x, command.y);
        crosshairs.SetColor(crossHairColor, 0.75f);
        transform.position = new Vector3(0, 100, 0);
        transform.localScale = new Vector3(15, 15, 15);
        transform.eulerAngles = new Vector3(15, 90, 0);
        lerperTransform = GetComponent<LerpTransform>();
        lerperTransform.SetTransform(targetSlot.transform, 1f);
        
	}

    protected override void Update()
    {
        base.Update();
        if(timeSinceFinish > 2)
        {
            Destroy(gameObject);
        }
    }

    public override void Finish()
    {
        base.Finish();
        crosshairs.SetColor(Color.clear, 1f);
    }
}
