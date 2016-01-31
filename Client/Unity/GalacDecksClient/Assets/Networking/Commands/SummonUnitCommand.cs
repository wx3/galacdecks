using System;
using System.Collections.Generic;

public class SummonUnitCommand : GameCommand, IPlayCardCommand {

    public int cardEntityId;
    public int x, y;

	public SummonUnitCommand(int id, EntityView card, UnitSlot slot) : base(id)
    {
        if(card.id <=0)
        {
            throw new System.Exception("Card was missing id");
        }
        cardEntityId = card.id;
        x = slot.x;
        y = slot.y;
    }

    public int CardEntityId
    {
        get
        {
            return cardEntityId;
        }
    }

    public override string CommandPrefabName
    {
        get
        {
            return "Play Card Command";
        }
    }
}
