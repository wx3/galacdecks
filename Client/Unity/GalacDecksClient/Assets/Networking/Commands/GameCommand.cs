using UnityEngine;
using System.Collections;

/// <summary>
/// Base class for Client commands to the server
/// </summary>
public class GameCommand : ClientRequest {

    public string commandClass;  

    /// <summary>
    /// If there's a command prefab, the command manager will instantiate
    /// it as a player cue until we receive acknowledgement.
    /// </summary>
    public virtual string CommandPrefabName
    {
        get
        {
            return null;
        }
    }

    public GameCommand(int id) : base()
    {

    }

    public GameCommand(GameClient.MessageHandler handler) : base(handler)
    {
        commandClass = this.GetType().Name;
    }
	
}
