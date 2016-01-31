using UnityEngine;
using System.Collections;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

public abstract class ClientRequest {

    public int ackId;
    public string requestClass;
    [JsonIgnoreAttribute]
    public GameClient.MessageHandler messageHandler;
    

    /// <summary>
    /// Does this request block the client from handling additional
    /// commands until the server has responded?
    /// </summary>
    public virtual bool Blocking
    {
        get
        {
            return true;
        }
    }

    public ClientRequest()
    {
        this.requestClass = this.GetType().Name;
    }

    /// <summary>
    /// If a ClientRequest specifies a message handler, that will be called
    /// when a message with the corresponding ack id is received.
    /// </summary>
    /// <param name="handler"></param>
    public ClientRequest(GameClient.MessageHandler handler)
    {
        this.requestClass = this.GetType().Name;
        this.messageHandler = handler;
    }

}
