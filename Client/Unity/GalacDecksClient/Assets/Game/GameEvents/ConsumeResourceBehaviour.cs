using UnityEngine;
using System.Collections.Generic;
using System;

public class ConsumeResourceBehaviour : GameEventBehaviour {

    public float fadeOut = 1;
    public GameObject energyFlyPrefab;
    public GameObject mineralFlyPrefab;
    public float flyDelay;

    private bool isPlayer;
    private int counter = 0;
    private float flyTimer = 0;

    private ConsumeResourceEvent data;
    private CardEntity card;
    private List<ResourceFly> flyIns = new List<ResourceFly>();

    public override GameEvent Data
    {
        set
        {
            this.data = (ConsumeResourceEvent)value;
        }
    }

    // Use this for initialization
    void Start () {
        card = GameManager.Instance.GetEntity(data.causeId) as CardEntity;
        if (data.playerName == GameManager.Instance.MyPlayer.playerName)
        {
            isPlayer = true;
        }
        else
        {
            isPlayer = false;
            if(data.resource == "ENERGY_RESOURCE")
            {
                UIManager.Instance.enemyEnergy.text = data.amount + " / " + GameManager.Instance.OpponentPlayer.GetMaxResource("ENERGY_RESOURCE");
            }
            else
            {
                UIManager.Instance.enemyMinerals.text = data.amount + " / " + GameManager.Instance.OpponentPlayer.GetMaxResource("MINERAL_RESOURCE");
            }
            
            Remove();
        }
        flyTimer = flyDelay;
    }
	
	override protected void Update () {
        base.Update();
        if (isPlayer) UpdateMyConsumption();
	}

    private void UpdateMyConsumption()
    {
        base.Update();
        bool showFlyIn = true;
        if (card.Alpha < 1) showFlyIn = false;
        if (counter < data.amount)
        {
            if (flyTimer >= flyDelay)
            {
                if(data.resource == "ENERGY_RESOURCE")
                {
                    Vector3 flySpawn = UIManager.Instance.energyStats.DeductResource();
                    if (showFlyIn)
                    {
                        GameObject go = Instantiate(energyFlyPrefab, flySpawn, Quaternion.identity) as GameObject;
                        ResourceFly fly = go.GetComponent<ResourceFly>();
                        fly.Attractor = card.resourceContainer.transform;
                        flyIns.Add(fly);
                    }
                }
                else
                {
                    Vector3 flySpawn = UIManager.Instance.mineralStats.DeductResource();
                    if(showFlyIn)
                    {
                        GameObject go = Instantiate(mineralFlyPrefab, flySpawn, Quaternion.identity) as GameObject;
                        ResourceFly fly = go.GetComponent<ResourceFly>();
                        fly.Attractor = card.resourceContainer.transform;
                        flyIns.Add(fly);
                    }
                }
                ++counter;
                flyTimer = 0;
            }
            else
            {
                flyTimer += Time.deltaTime;
            }
        }
        else
        {
            int activeFlys = 0;
            foreach (ResourceFly fly in flyIns)
            {
                if (!fly.Done)
                {
                    ++activeFlys;
                }
            }
        }
    }

}
