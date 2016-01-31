using UnityEngine;
using System.Collections;

public class Shields : MonoBehaviour {

    public LerpTint shields1;
    public LerpTint shields2;

    public Color shield1Color;
    public Color shield2Color;
    public int shieldPeakValue;

    public GameObject shieldFlash;
    public GameObject shieldBurnPrefab;
    public AnimationCurve flashCurve;
    public Color flashColor;

    public AudioSource audiosource;
    public AudioClip shieldHitSound;
    public AudioClip shieldsUpSound;
    public AudioClip shieldsDownSound;

    private int strength = 0;
    private Renderer _flashRenderer;
    private float timer = 0;

    public int Strength
    {
        get
        {
            return strength;
        }
        set
        {
            if(strength != value)
            {
                if(strength == 0)
                {
                    audiosource.PlayOneShot(shieldsUpSound);
                } else if(strength > 0 && value == 0)
                {
                    Debug.Log("Shield strength: " + strength);
                    audiosource.PlayOneShot(shieldsDownSound);
                }
                strength = value;
                if (strength > 0)
                {
                    float amount = Mathf.Lerp(0, 1, (float)strength / (float)shieldPeakValue);
                    shields1.SetColor(Color.Lerp(Color.clear, shield1Color, amount), 1);
                    shields2.SetColor(shield2Color, 1);
                }
                else
                {
                    shields1.SetColor(Color.clear, 1);
                    shields2.SetColor(Color.clear, 1);
                }
            }
        }
    }

    void Awake()
    {
        audiosource = GetComponent<AudioSource>();
    }

    void Start()
    {
        shields1.SetColor(Color.clear, 0);
        shields2.SetColor(Color.clear, 0);
        _flashRenderer = shieldFlash.GetComponent<Renderer>();
        if (shieldPeakValue < 1) shieldPeakValue = 1;
        timer = 9999;
    }

    void Update()
    {
        timer = timer + Time.deltaTime;
        float flashStrength = flashCurve.Evaluate(timer);
        _flashRenderer.material.SetColor("_TintColor", Color.Lerp(Color.clear, flashColor, flashStrength));
    }

    public void ShieldHit(DamageEffect damage)
    {
        audiosource.PlayOneShot(shieldHitSound);
        Strength = Strength - damage.shieldBlocked;
    }

    void OnTriggerEnter(Collider collision)
    {
        Projectile projectile = collision.gameObject.GetComponent<Projectile>();
        if(projectile.Damage.shieldBlocked > 0)
        {
            ShieldHit(projectile);
        }
    }

    private void ShieldHit(Projectile projectile)
    {
        audiosource.PlayOneShot(shieldHitSound);
        shieldFlash.gameObject.SetActive(true);
        shieldFlash.transform.LookAt(projectile.transform);
        Instantiate(shieldBurnPrefab, projectile.transform.position, Quaternion.identity);
        timer = 0;
    }

}
