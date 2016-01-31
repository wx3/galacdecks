using UnityEngine;
using System.Collections.Generic;

public class JoinMessage {

    public int serverVersion;
    public long gameId;
    public int position;
    public string shipPrefab;
    public int columns;
    public int rows;
    public List<EntityCoordinates> mySlots;

}
