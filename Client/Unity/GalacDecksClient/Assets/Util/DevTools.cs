using UnityEngine;
using System.Collections;

public class DevTools : MonoBehaviour {
	
	void Update () {
	    if(Input.GetKeyDown(KeyCode.KeypadPlus))
        {
            Time.timeScale = Time.timeScale * 1.1f;
            Debug.Log("Time Scale: " + Time.timeScale);
        }
        if(Input.GetKeyDown(KeyCode.KeypadMinus))
        {
            Time.timeScale = Time.timeScale * 0.9f;
            Debug.Log("Time Scale: " + Time.timeScale);
        }
        if(Mathf.Abs(Time.timeScale - 1) < 0.01f)
        {
            Time.timeScale = 1;
        }
        if(Input.GetKeyDown(KeyCode.F1))
        {
            ListRaycasts();
        }
	}

    private void ListRaycasts()
    {
        Ray ray = Camera.main.ScreenPointToRay(Input.mousePosition);
        RaycastHit[] hits;
        hits = Physics.RaycastAll(ray);
        int i = 0;
        while (i < hits.Length)
        {
            RaycastHit hit = hits[i];
            Debug.Log(hit.collider.gameObject.name);
            i++;
        }
    }
}
