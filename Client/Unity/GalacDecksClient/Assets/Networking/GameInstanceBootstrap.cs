using UnityEngine;
using System.Collections;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

public class GameInstanceBootstrap : MonoBehaviour {

	void Start () {
        GameClient.Instance.defaultMessageHandler = GameManager.Instance.HandleMessage;
        GameManager.Instance.gameOverHandler = OnGameOver;
        GameClient.Instance.Connect(OnConnect);
	}

    public void OnConnect()
    {
        Debug.Log("Connected..");
        ClientRequest request = new CreateGuestRequest(OnGuestCreated);
        GameClient.Instance.SendRequest(request);
    }

    public void OnGuestCreated(JObject jsonObject)
    {
        Debug.Log("Guest created: " + jsonObject);
        StarSystemData data = new StarSystemData();
        string starSystemId = GameManager.Instance.starSystemEnvironment.name;
        data.id = starSystemId;
        ClientRequest request = new NewGameRequest(OnGameCreated, data);
        GameClient.Instance.SendRequest(request);
    }

    public void OnGameCreated(JObject jsonObject)
    {
        Debug.Log("Game created: " + jsonObject);
        GameCreatedMessage message = jsonObject.ToObject<GameCreatedMessage>();
        long id = message.gameId;
        JoinRequest request = new JoinRequest(OnGameJoined, id);
        GameClient.Instance.SendRequest(request);
    }

    public void OnGameJoined(JObject jsonObject)
    {
        Debug.Log("Game joined: " + jsonObject);
        JoinMessage message = jsonObject.ToObject<JoinMessage>();
        int serverVersion = message.serverVersion;
        if(serverVersion != GameClient.Instance.clientVersion)
        {
            Debug.LogWarning("Client and server version mismatch: " + GameClient.Instance.clientVersion + " != " + serverVersion);
        }
        GameClient.Instance.sceneTransition.TransitionIn();
        GameManager.Instance.HandleJoin(message);
    }

    public void OnMessage(JObject jsonObject)
    {
        Debug.Log("Message: " + jsonObject);
    }

    public void OnGameOver()
    {
        Debug.Log("Game over");
    }

}
