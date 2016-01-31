using UnityEngine;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System.Collections;
using System.Collections.Generic;

public class GameManager : Singleton<GameManager> {

    public string playerName;

    public PlayerHand playerHand;
    public PlayerHand opponentHand;

    public Gameboard gameBoard;
    public GameEventQueue gameEventQueue;
    public ScriptedDialogBox scriptedDialogBox;
    public Transform starSystemContainer;
    public StarSystemEnvironment starSystemEnvironment;
    
    /// <summary>
    /// If the opponent/AI joins quickly, we still wait this long before starting
    /// their entrance.
    /// </summary>
    public float minStartDelay;

    /// <summary>
    /// Modal dialogs are added to this container, which should be in front of 
    /// everything but the advisor.
    /// </summary>
    public RectTransform dialogContainer;

    // Not actually used by the game, just for debugging in the editor:
    public List<GameEntity> entities = new List<GameEntity>();

    // This should be the state of the game. It's initialized when the client
    // joins, and is updated by GameEvents from the server:
    public GameView gameView;

    private bool ready = false;

    public delegate void OnGameOver();
    public OnGameOver gameOverHandler;

    private PrefabStore prefabStore;
    // This is how we lookup entities:
    private Dictionary<int, GameEntity> entityLookup = new Dictionary<int, GameEntity>();
    private Dictionary<string, PrototypeData> cardLookUp;

    private float delayTimer = 0;

    protected GameManager() { }

    private Dictionary<string, PrototypeData> Cards
    {
        get
        {
            return cardLookUp;
        }
        set
        {
            cardLookUp = value;
        }
    }

    void Start()
    {
        prefabStore = GetComponent<PrefabStore>();
    }

    /// <summary>
    /// Is that game started and ready for events?
    /// </summary>
    public bool IsReady
    {
        get
        {
            return ready;
        }
        set
        {
            ready = value;
        }
    }

    public int Turn
    {
        get
        {
            return gameView.turn;
        }
        set
        {
            gameView.turn = value;
        }
    }

    public bool IsMyTurn
    {
        get
        {
            if (string.IsNullOrEmpty(gameView.viewer)) return false;
            return gameView.viewer == gameView.currentPlayer;
        }
    }

    public PlayerView MyPlayer
    {
        get
        {
            if (gameView != null && gameView.players.Count > 0) return gameView.GetMyPlayerView();
            return null;
        }
    }

    public PlayerView OpponentPlayer
    {
        get
        {
            if(gameView.viewerPosition == 1)
            {
                return gameView.players[1];
            }
            else
            {
                return gameView.players[0];
            }
        }
    }

    void Update()
    {
        if (Input.GetKeyDown(KeyCode.Escape))
        {
            GameClient.Instance.applicationUi.ShowDialog("Concede?", UIManager.Instance.quitDialogPrefab);
        }
        delayTimer += Time.deltaTime;
    }

    public void SetStarSystem(string prefabId)
    {
        GameObject prefab = AssetManager.Instance.GetStarSystem(prefabId);
        if (prefab == null)
        {
            prefab = AssetManager.Instance.GetStarSystem("DEFAULT");
        }
        foreach (Transform child in starSystemContainer.transform)
        {
            Destroy(child.gameObject);
        }
        GameObject go = Instantiate(prefab);
        go.transform.position = Vector3.zero;
        go.transform.parent = starSystemContainer.transform;
    }

    public void HandleMessage(JObject json)
    {
        string messageClass = (string)json["messageClass"];
        switch(messageClass)
        {
            case "JoinMessage":
                JoinMessage join = json.ToObject<JoinMessage>();
                HandleJoin(join);
                break;
            case "GameInitMessage":
                GameInitMessage gameInit = json.ToObject<GameInitMessage>();
                HandleGameInit(gameInit);
                break;
            case "ValidPlaysMessage":
                ValidPlaysMessage valids = json.ToObject<ValidPlaysMessage>();
                HandleValidPlays(valids);
                break;
            case "EventsMessage":
                EventsMessage eventsMsg = new EventsMessage();
                eventsMsg.commandPlayer = json["commandPlayer"].ToString();
                eventsMsg.ackId = json["ackId"].ToObject<int>();
                eventsMsg.updatedView = json["updatedView"].ToObject<GameView>();
                JEnumerable<JToken> eventList = json["events"].Children();
                foreach (JToken token in eventList)
                {
                    GameEvent gameEvent = EventFactory.FromJToken(token);
                    if (gameEvent != null)
                    {
                        eventsMsg.events.Add(gameEvent);
                    }
                }
                HandleEventsMessage(eventsMsg);
                break;
            default:
                Debug.LogError("Unknown message: " + messageClass);
                break;
        }
    }

    public void HandleJoin(JoinMessage message)
    {
        Debug.Log("Join message received: " + message);
        int serverVersion = message.serverVersion;
        if (serverVersion != GameClient.Instance.clientVersion)
        {
            Debug.LogWarning("Client and server version mismatch: " + GameClient.Instance.clientVersion + " != " + serverVersion);
        }

        GameClient.Instance.sceneTransition.Message = "Engage!";
        GameClient.Instance.sceneTransition.startDelay = 0;
        GameClient.Instance.sceneTransition.TransitionIn();
    }

    public void InitializeHands()
    {
        int myPos = gameView.viewerPosition;
        int oppPos;
        if (myPos == 1) { oppPos = 2; }
        else { oppPos = 1; }
        playerHand.InitializeFromView(gameView, myPos);
        opponentHand.InitializeFromView(gameView, oppPos);
    }

    private void HandleGameInit(GameInitMessage message)
    {
        float wait = minStartDelay - delayTimer;
        if (wait < 0) wait = 0;
        StartCoroutine(DelayedInit(message, wait));
    }

    // Delay the game initialization so the player sees his ship flying:
    private IEnumerator DelayedInit(GameInitMessage message, float delay)
    {
        yield return new WaitForSeconds(delay);
        Debug.Log("Initializing game...");
        cardLookUp = message.cards;
        GameView gameView = message.gameView;
        this.gameView = gameView;
        playerName = gameView.viewer;
        gameBoard.InitializeFromView(gameView);
        RefreshResources();
        starSystemEnvironment.GameStarted();
        UpdateView(gameView);
    }

    private void HandleValidPlays(ValidPlaysMessage message)
    {
        Debug.Log(message);
        gameEventQueue.UpdatedPlays = message.validPlays;
    }

    private void HandleEventsMessage(EventsMessage message)
    {
        if(message.commandPlayer == GameManager.Instance.playerName)
        {
            CommandManager.Instance.AckReceived(message.ackId);
        }
        foreach(GameEvent gameEvent in message.events)
        {
            gameEventQueue.Add(gameEvent);
        }
        gameEventQueue.UpdatedView = message.updatedView;
    }

    public void UpdateView(GameView gameView)
    {
        this.gameView = gameView;
        foreach(EntityView ev in gameView.inPlay)
        {
            if(ev.name != "ROOT")
            {
                GameEntity entity = GetEntity(ev.id);
                if (entity != null)
                {
                    entity.EntityView = ev;
                }
                else
                {
                    Debug.LogWarning("Entity " + ev.name + "(" + ev.id + ") missing");
                }
            }
        }
        RefreshResources();
        UIManager.Instance.energyStats.Max = MyPlayer.GetMaxResource("ENERGY_RESOURCE");
        UIManager.Instance.mineralStats.Max = MyPlayer.GetMaxResource("MINERAL_RESOURCE");
        UIManager.Instance.cardStats.Value = MyPlayer.deckSize;
    }

    private void RefreshResources()
    {
        UIManager.Instance.energyStats.Value = MyPlayer.GetResource("ENERGY_RESOURCE");
        UIManager.Instance.energyStats.Max = MyPlayer.GetMaxResource("ENERGY_RESOURCE");

        UIManager.Instance.mineralStats.Value = MyPlayer.GetResource("MINERAL_RESOURCE");
        UIManager.Instance.mineralStats.Max = MyPlayer.GetMaxResource("MINERAL_RESOURCE");

        int opponentEnergy = OpponentPlayer.GetResource("ENERGY_RESOURCE");
        int opponentMaxEnergy = OpponentPlayer.GetMaxResource("ENERGY_RESOURCE");
        int opponentMineral = OpponentPlayer.GetResource("MINERAL_RESOURCE");
        int opponentMaxMineral = OpponentPlayer.GetMaxResource("MINERAL_RESOURCE");

        UIManager.Instance.enemyEnergy.text = opponentEnergy + " / " + opponentMaxEnergy;
        UIManager.Instance.enemyMinerals.text = opponentMineral + " / " + opponentMaxMineral;
        UIManager.Instance.enemyCardStats.Value = OpponentPlayer.deckSize;
    }

    public PrototypeData GetPrototype(string prototypeName)
    {
        if(cardLookUp.ContainsKey(prototypeName))
        {
            return cardLookUp[prototypeName];
        }
        Debug.LogWarning("No such prototype '" + prototypeName + "'");
        return null;
    }

    public GameObject SpawnCard(string prefabName, EntityView entityData)
    {
        GameObject prefab = prefabStore.GetPrefab(prefabName);
        if (prefab == null)
        {
            throw new System.Exception("Could not find prefab '" + prefabName + "'");
        }
        GameObject go = Instantiate(prefab);
        GameEntity entity = go.GetComponent<GameEntity>();
        if(entity == null)
        {
            Debug.LogError("Entity prefab lacked " + typeof(GameEntity).ToString() + " component");
            Destroy(go);
            return null;
        }
        entity.EntityView = entityData;
        AddEntity(entity);
        return go;
    }

    public UnitEntity SpawnUnit(EntityView entityView)
    {
        if(!cardLookUp.ContainsKey(entityView.prototype))
        {
            throw new System.Exception("Could not find a prototype '" + entityView.prototype);
        }
        PrototypeData prototype = cardLookUp[entityView.prototype];
        if(string.IsNullOrEmpty(prototype.unitPrefab))
        {
            throw new System.Exception("Prototype does not have a unitPrefab associated with it.");
        }
        GameObject prefab = AssetManager.Instance.GetUnitPrefab(prototype.unitPrefab);
        if(prefab == null)
        {
            throw new System.Exception("Could not find prefab '" + prototype.unitPrefab + "'");
        }
        GameObject go = Instantiate(prefab);
        UnitEntity unit = go.GetComponent<UnitEntity>();
        unit.EntityView = entityView;
        AddEntity(unit);
        if(unit == null)
        {
            Destroy(go);
            throw new System.Exception("Unit prefab did not have UnitEntity component");
        }
        return unit;
    }

    public void AddEntity(GameEntity entity)
    {
        int id = entity.EntityView.id;
        entityLookup[id] = entity;
        entities.Add(entity);
    }

    public GameEntity GetEntity(int id)
    {
        if(!entityLookup.ContainsKey(id))
        {
            Debug.LogWarning("No such entity with id " + id);
            return null;
        }
        return entityLookup[id];
    }

    public GameEntity GetHomeworld(string owner)
    {
        PlayerView pv = gameView.getPlayerView(owner);
        return GetEntity(pv.homeworldId);
    }

    public bool RemoveEntity(int id)
    {
        if(entityLookup.ContainsKey(id))
        {
            GameEntity e = entityLookup[id];
            entities.Remove(e);
            entityLookup.Remove(id);
            return true;
        }
        return false;
    }

}
