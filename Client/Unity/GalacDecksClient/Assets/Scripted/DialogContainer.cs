using UnityEngine;
using UnityEngine.UI;
using System.Collections;

public class DialogContainer : MonoBehaviour {

    public Text dialogText;
    public Image portrait;
    /// <summary>
    /// If there's a control area that players can use to dismiss the dialog, disable it if this does
    /// not allow dismissing.
    /// </summary>
    public Transform dismissContainer;

    public ScriptedDialog Dialog
    {
        set
        {
            dialogText.text = value.text;
            if(value.allowDismiss)
            {
                dismissContainer.gameObject.SetActive(true);
            }
            else
            {
                dismissContainer.gameObject.SetActive(false);
            }
        }
    }
}
