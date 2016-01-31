using UnityEngine;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System.Collections.Generic;

public class StarSystemsScreen : MonoBehaviour {

    public Transform starSystemsContainer;
    public StarSystemDetails starSystemDetails;
    public StarSystemsCamera starCamera;
    public float minimumTransition = 1;

    private Dictionary<string, StarSystem> starSystems = new Dictionary<string, StarSystem>();
    private float elapsed = 999;
    private StarSystem joinSystem;
    private bool joined = false;

	void Start () {
	    foreach(Transform child in starSystemsContainer)
        {
            StarSystem starSystem = child.GetComponentInChildren<StarSystem>();
            if(starSystem != null)
            {
                string id = starSystem.data.id;
                starSystems[id] = starSystem;
                starSystem.starSystemsDetails = starSystemDetails;
            }
        }
        starSystemDetails.onJoinClick = OnJoin;
        if(!GameClient.Instance.IsConnected)
        {
            GameClient.Instance.Connect(OnConnect);
        }
    }

	void Update () {
        elapsed += Time.deltaTime;
        if(elapsed > minimumTransition && joinSystem != null)
        {
            GameClient.Instance.JoinSystem(joinSystem.data);
        }
        // Reload this screen for testing:
        if (Input.GetKeyDown(KeyCode.F5))
        {
            Application.LoadLevel(Application.loadedLevel);
            GameClient.Instance.sceneTransition.TransitionIn();
        }
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
        GameClient.Instance.sceneTransition.TransitionIn();
    }

    void OnJoin(StarSystem starSystem)
    {
        Debug.Log("Joining " + starSystem);
        if(starSystem.data.id == "")
        {
            Debug.LogError("Missing star system id");
            return;
        }
        joinSystem = starSystem;
        starCamera.TargetSystem = starSystem;
        GameClient.Instance.sceneTransition.startDelay = 2.25f;
        GameClient.Instance.sceneTransition.TransitionOut();
        GameClient.Instance.sceneTransition.Message = "Joining " + starSystem.data.name;
        elapsed = 0;
    }

}
