using UnityEngine;
using System.Collections;

public class NewGameRequest : ClientRequest {

    public string systemId;

    public NewGameRequest(GameClient.MessageHandler handler, StarSystemData data) : base(handler)
    {
        this.systemId = data.id;
    }
}
