using UnityEngine;
using UnityEngine.UI;
using System.Collections;


public class DialogBox : MonoBehaviour {

    public Text titleText;
    public RectTransform contentArea;
    public float transitionTime;

    private CanvasGroup canvasGroup;
    private GameObject content;
    private float transitionTimer;
    private bool closed = false;

    public string Title
    {
        get
        {
            return titleText.text;
        }
        set
        {
            titleText.text = value;
        }
    }

    public GameObject Content
    {
        get
        {
            return content;
        }
        set
        {
            if(content != null)
            {
                Destroy(content);
            }
            content = value;
            if(content != null)
            {
                BaseDialogContent dialog = content.GetComponent<BaseDialogContent>();
                dialog.DialogBox = this;
                content.transform.SetParent(contentArea);
            }
        }
    }

    public void Close()
    {
        closed = true;
        transitionTimer = 0;
    }

    void Awake()
    {
        canvasGroup = GetComponent<CanvasGroup>();
        canvasGroup.alpha = 0;
    }

	void Start () {
        canvasGroup.alpha = 0;
    }
	
	void Update () {
        transitionTimer += Time.deltaTime;
        if(!closed)
        {
            canvasGroup.alpha = Mathf.Lerp(0, 1, transitionTimer);
        }
        else
        {
            canvasGroup.alpha = Mathf.Lerp(1, 0, transitionTimer);
            if(canvasGroup.alpha <= 0)
            {
                Destroy(gameObject);
            }
        }
	}
}
