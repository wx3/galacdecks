using UnityEngine;
using System.Collections;

public class OptionsButton : MonoBehaviour {

    public GameObject optionsDialogPrefab;

	public void ShowOptions()
    {
        GameClient.Instance.applicationUi.ShowDialog("Options", optionsDialogPrefab);
    }
}
