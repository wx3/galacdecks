using UnityEngine;
using System.Collections;
using System;

public class PlayCardCommandBehaviour : CommandBehaviour {

    public float enlargeTime;
    public Transform cardEnlarged;
    public float particleEmitDuration;

    private CardEntity card;
    private IPlayCardCommand command;
    private ParticleSystem[] particleSystems;

    public override GameCommand Command
    {
        get
        {
            return (GameCommand) command;
        }

        set
        {
            command = (IPlayCardCommand)value;
        }
    }

    void Awake()
    {
        particleSystems = GetComponentsInChildren<ParticleSystem>();
    }

    void Start()
    {
        card = GameManager.Instance.GetEntity(command.CardEntityId).GetComponent<CardEntity>();
        transform.position = card.transform.position;
        card.lerpTransform.SetTransform(cardEnlarged, enlargeTime);
    }

    protected override void Update()
    {
        base.Update();
        if(finished && timeSinceStart > particleEmitDuration)
        {
            foreach (ParticleSystem ps in particleSystems)
            {
                ps.enableEmission = false;
            }
        }
    }

}
