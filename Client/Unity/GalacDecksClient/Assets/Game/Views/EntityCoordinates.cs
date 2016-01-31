using System;

public struct EntityCoordinates {

	public int x, y;

    public EntityCoordinates(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public override string ToString()
    {
        return "EntityCoordinates " + x + "," + y;
    }

}
