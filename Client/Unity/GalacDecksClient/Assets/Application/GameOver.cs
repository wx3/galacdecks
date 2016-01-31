using UnityEngine;
using System.Collections;

public class GameOver : BaseDialogContent {

	override public void Close()
    {
        base.Close();
        Destroy(GameManager.Instance.gameObject);
    }

    void OnDestroy()
    {
        //ApplicationManager.Instance.MainMenu();
    }
}
