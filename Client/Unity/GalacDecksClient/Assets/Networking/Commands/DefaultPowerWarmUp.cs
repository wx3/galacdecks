using UnityEngine;
using System.Collections;
using System;

public class DefaultPowerWarmUp : CommandBehaviour {

    public AudioSource audioSource;

    private PlayPowerCommand command;

    public override GameCommand Command
    {
        get
        {
            return command;
        }

        set
        {
            command = (PlayPowerCommand)value;
        }
    }

    protected override void Update()
    {
        base.Update();
        if(finished)
        {
            audioSource.volume -= 0.05f;
        }
        if (timeSinceFinish > 0.5f)
        {
            Destroy(gameObject);
        }
    }
}
