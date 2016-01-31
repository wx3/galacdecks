using UnityEngine;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.Collections;
using System.Collections.Generic;

/// <summary>
/// The GameClient is the main controller for the application's interaction with the server.
/// </summary>
public class GameClient : Singleton<GameClient>
{
    private static int ID_COUNTER = 1;

    public int clientVersion;

    public ClientConfig configuration;
    public SceneTransition sceneTransition;
    public ApplicationUI applicationUi;

    public delegate void MessageHandler(JObject jsonObject);
    public GameClient.MessageHandler defaultMessageHandler;

    private WebSocketBridge connection;
    
    private Dictionary<int, ClientRequest> awaitingAck = new Dictionary<int, ClientRequest>();

    public bool IsConnected
    {
        get
        {
            if(connection != null) return connection.IsConnected;
            return false;
        }
    }

    override protected void Awake()
    {
        base.Awake();
        connection = GetComponent<WebSocketBridge>();
        connection.simulatedLatency = configuration.simulatedLatency;
        connection.errorHandler = OnConnectionError;
        connection.closeHandler = OnConnectionClose;
    }

    void Start()
    {
        sceneTransition.Message = "Connecting...";
    }

    public void Connect(WebSocketBridge.OnConnect connectHandler)
    {
        connection.onTextMessage = OnTextMessage;
        connection.Connect(configuration.host);
        connection.connectHandler = connectHandler;
    }

    /// <summary>
    /// Attaches an ackId to an outbound request so we can match a response to its request
    /// and sends it to the WebSocketBridge.
    /// </summary>
    /// <param name="request"></param>
    public void SendRequest(ClientRequest request)
    {
        foreach (KeyValuePair<int, ClientRequest> entry in awaitingAck)
        {
            if (entry.Value.Blocking)
            {
                throw new Exception("Cannot send request, still awaiting ack on " + entry.Value);
            }
        }
        request.ackId = ++ID_COUNTER;
        awaitingAck[request.ackId] = request;
        string encoded = JsonConvert.SerializeObject(request);
        connection.Send(encoded);
    }

    private void OnConnect()
    {
        sceneTransition.Message = "Connected/";
        CreateGuest();
    }

    private void OnConnectionClose()
    {
        Debug.Log("Disconnected.");
    }

    private void OnConnectionError(string message)
    {
        Debug.LogError("Connection error: " + message);
        applicationUi.Alert("Error: " + message);
    } 

    private void OnTextMessage(string message)
    {
        JObject json = JObject.Parse(message);
        GameClient.MessageHandler handler = null;
        if (json["ackId"] != null)
        {
            int ackId = (int)json["ackId"];
            if (ackId > 0)
            {
                if (awaitingAck.ContainsKey(ackId))
                {
                    ClientRequest request = awaitingAck[ackId];
                    handler = request.messageHandler;
                    Debug.Log("Received ack for " + request);
                    awaitingAck.Remove(ackId);
                }
                else
                {
                    Debug.LogWarning("Received ack for unknown request: " + ackId);
                }
            }
        }
        if (handler != null)
        {
            handler(json);
        }
        else
        {
            defaultMessageHandler(json);
        }
    }

    public void CreateGuest()
    {
        ClientRequest request = new CreateGuestRequest(OnGuestCreated);
        SendRequest(request);
    }

    public void OnGuestCreated(JObject jsonObject)
    {   
        sceneTransition.Message = "Guest account created";
        sceneTransition.TransitionIn();
    }

    public void JoinSystem(StarSystemData starSystemData)
    {
        Application.LoadLevel("GameInstance");
        NewGameRequest request = new NewGameRequest(OnGameCreated, starSystemData);
        GameClient.Instance.SendRequest(request);
    }

    private void OnGameCreated(JObject jsonObject)
    {
        defaultMessageHandler = GameManager.Instance.HandleMessage;
        GameManager.Instance.gameOverHandler = GameOver;
        GameCreatedMessage message = jsonObject.ToObject<GameCreatedMessage>();
        long id = message.gameId;
        GameManager.Instance.SetStarSystem(message.starSystemId);
        JoinRequest request = new JoinRequest(OnGameJoined, id);
        GameClient.Instance.SendRequest(request);
    }


    private void GameOver()
    {
        sceneTransition.Message = "Returning to Hyperspace...";
        sceneTransition.startDelay = 0;
        sceneTransition.transitionDuration = 1;
        sceneTransition.TransitionOut();
        StartCoroutine(ReturnToStarSystems());
        
    }

    private IEnumerator ReturnToStarSystems()
    {
        yield return new WaitForSeconds(1);
        Application.LoadLevel("StarSystems");
        sceneTransition.startDelay = 0;
        sceneTransition.transitionDuration = 1;
        sceneTransition.TransitionIn();
    }


    private void OnGameJoined(JObject jsonObject)
    {
        JoinMessage message = jsonObject.ToObject<JoinMessage>();
        GameManager.Instance.HandleJoin(message);
    }


}
