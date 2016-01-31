using UnityEngine;
using System.Collections;

public class HelpArrow : MonoBehaviour {

    public float bounceDist;

    private GameObject target;
    private Canvas canvas;
    private RectTransform rect;
    private CanvasGroup canvasGroup;

    public GameObject Target
    {
        get
        {
            return target;
        }
        set
        {
            target = value;
        }
    }

    void Awake()
    {
        canvasGroup = GetComponent<CanvasGroup>();
    }

    void Start()
    {
        canvas = gameObject.GetComponentInParent<Canvas>();
        rect = GetComponent<RectTransform>();
    }

    void Update()
    {
        if(target == null)
        {
            if(canvasGroup.alpha > 0)
            {
                canvasGroup.alpha -= Time.deltaTime;
            }
        }
        else
        {
            if(canvasGroup.alpha < 1)
            {
                canvasGroup.alpha += Time.deltaTime;
            }
            float amount = UIManager.Instance.PingPong;
            float dist = bounceDist * amount;
            Vector3 worldPos = target.transform.position;
            Vector3 screenPos = Camera.main.WorldToScreenPoint(worldPos);
            transform.position = new Vector3(screenPos.x - dist, screenPos.y, screenPos.z);
        }
    }
}
