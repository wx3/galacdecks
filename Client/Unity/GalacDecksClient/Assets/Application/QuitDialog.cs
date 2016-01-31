using UnityEngine;
using System.Collections;

public class QuitDialog : BaseDialogContent {

	public void Quit()
    {
        CommandManager.Instance.Concede();
        Close();
    }
}
