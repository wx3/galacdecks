using UnityEngine;
using System.Collections;

public class DiscardCommand : GameCommand {

    public int cardEntityId;
    public int x, y;

    public DiscardCommand(int id, EntityView card, UnitSlot slot) : base(id)
    {
        this.cardEntityId = card.id;
        if (slot != null)
        {
            this.x = slot.x;
            this.y = slot.y;
        }
        else
        {
            this.x = -1;
            this.y = -1;
        }

    }

    public int CardEntityId
    {
        get
        {
            return cardEntityId;
        }
    }

   
}
