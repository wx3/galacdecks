using UnityEngine;
using System.Collections;

public class EndTurnCommand : GameCommand {

    public bool energy, mineral, draw;

	public EndTurnCommand(int id, bool energy, bool mineral, bool draw) : base(id)
    {
        this.energy = energy;
        this.mineral = mineral;
        this.draw = draw;
    }
}
