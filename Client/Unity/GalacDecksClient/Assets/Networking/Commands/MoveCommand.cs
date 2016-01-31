using UnityEngine;
using System.Collections;

public class MoveCommand : GameCommand {

    public int entityId;
    public int x, y;

    public MoveCommand(int id, EntityView unit, int x, int y) : base(id)
    {
        this.entityId = unit.id;
        this.x = x;
        this.y = y;
    }
	
}
