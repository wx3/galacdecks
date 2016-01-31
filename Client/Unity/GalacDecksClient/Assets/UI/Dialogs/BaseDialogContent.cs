using UnityEngine;
using System.Collections;

public abstract class BaseDialogContent : MonoBehaviour {

    private DialogBox dialogBox;

    public DialogBox DialogBox
    {
        set
        {
            dialogBox = value;
        }
    }

    public virtual void Close()
    {
        dialogBox.Close();
    }
}
