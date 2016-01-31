using UnityEngine;
using UnityEngine.UI;
using System.Collections;

public class DamageCounter : MonoBehaviour {

    public float fadeTime = 1.5f;

    private int damage;

    private Text text;
    private Canvas canvas;
    private CanvasGroup canvasGroup;
    private float elapsed = 0;
    private ParticleSystem[] particles;

    public int Damage
    {
        set
        {
            damage = value;
            text.text = "-" + value.ToString();
        }
    }

	void Awake () {
        canvasGroup = GetComponentInChildren<CanvasGroup>();
        text = GetComponentInChildren<Text>();
        particles = GetComponentsInChildren<ParticleSystem>();
	}

    void Start()
    {
        foreach(ParticleSystem ps in particles)
        {
            ps.startSize = 15 + (damage * 2);
            ps.startSpeed = 5 + (damage * 3);
            ps.emissionRate = 100 + (50 * damage);
        }
    }
	
	void Update () {
        elapsed += Time.deltaTime;
        canvasGroup.alpha = Mathf.Lerp(1, 0, elapsed / fadeTime);
        transform.position += new Vector3(0, 10 * Time.deltaTime, 0);
    }
}
