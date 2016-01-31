using UnityEngine;
using System.Collections;

public class AttackCommand : GameCommand {

    public int attackerId;
    public int x, y;

	public AttackCommand(int id, EntityView attacker, EntityView target) : base(id)
    {
        this.attackerId = attacker.id;
        this.x = target.column;
        this.y = target.row;
    }

    public override string CommandPrefabName
    {
        get
        {
            return "AttackCommand";
        }
    }
}
