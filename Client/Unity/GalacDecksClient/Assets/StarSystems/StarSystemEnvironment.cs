using UnityEngine;
using System.Collections;
using System.Collections.Generic;

/// <summary>
/// Renders the distinct star system for this level, handles loading transition.
/// </summary>
public class StarSystemEnvironment : MonoBehaviour {

    public Transform dialogScriptContainer;
    public Transform cameraEnvironment;

    public List<ParallaxBox> parallaxes;
    public List<TextureShift> textureShifts;

    public AnimationCurve transitionCurve;
    public float maxSpeed;

    private float speed = 0;
    private float transitionTimer = 999;

    public float Speed
    {
        get
        {
            return speed;
        }
        set
        {
            speed = value;
            foreach(TextureShift t in textureShifts)
            {
                t.apparentSpeed = new Vector2(speed, 0);
            }
            foreach(ParallaxBox p in parallaxes)
            {
                p.apparentSpeed = new Vector3(speed, 0, 0);
            }
        }
    }


    void Start()
    {
        GameManager.Instance.starSystemEnvironment = this;
        if(dialogScriptContainer != null)
        {
            GameManager.Instance.scriptedDialogBox.activeScripts = dialogScriptContainer;
        }
        cameraEnvironment.transform.parent = Camera.main.transform;
        Speed = 0;
    }

    public void GameStarted()
    {
        StartCoroutine(GameReady(1));
    }

    private IEnumerator GameReady(float delay)
    {
        yield return new WaitForSeconds(delay);
        GameManager.Instance.InitializeHands();
        GameManager.Instance.IsReady = true;
        UIManager.Instance.CanvasVisible = true;
    }

}
