using UnityEngine;
using UnityEngine.UI;
using System.Collections;

public class AlertDialog : BaseDialogContent {

    [SerializeField]
    private Text text;

    public string Text
    {
        get
        {
            return text.text;
        }
        set
        {
            text.text = value;
        }
    }
}
