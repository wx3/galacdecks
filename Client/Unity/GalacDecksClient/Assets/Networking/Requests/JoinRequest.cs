using UnityEngine;
using System.Collections;

public class JoinRequest : ClientRequest {

    public long gameId;

    public JoinRequest(GameClient.MessageHandler handler, long gameId) : base(handler)
    {
        this.gameId = gameId;
    }
}
