using UnityEngine;
using UnityEngine.UI;
using System;
using System.Collections;

public class ApplicationUI : MonoBehaviour {

    public GameObject dialogPrefab;
    public GameObject alertPrefab;

    public Text consoleText;

    private Canvas canvas;

    void Awake()
    {
        canvas = GetComponentInChildren<Canvas>();
    }

    public void Alert(string message, string title = "Alert")
    {
        GameObject go = ShowDialog(title, alertPrefab);
        AlertDialog alert = go.GetComponent<AlertDialog>();
        alert.Text = message;
    }

    public GameObject ShowDialog(string title, GameObject contentPrefab)
    {
        GameObject dialog = Instantiate(dialogPrefab); 
        dialog.transform.SetParent(canvas.transform);
        RectTransform dialogRect = dialog.GetComponent<RectTransform>();
        // This will center the dialog on the canvas' pivot point
        dialogRect.anchoredPosition = Vector2.zero;
        GameObject content = Instantiate(contentPrefab);

        DialogBox dialogInstance = dialog.GetComponent<DialogBox>();
        dialogInstance.Title = title;
        dialogInstance.Content = content;
        return content;
    }
    
}
