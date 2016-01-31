using UnityEngine;
using UnityEngine.UI;
using System.Collections;

public class AssetManager : Singleton<AssetManager> {

	public Sprite GetPortrait(string name)
    {
        string path = "portraits/" + name;
        Object thing = Resources.Load(path);
        if(thing == null)
        {
            Debug.LogWarning(path + " not found");
        }
        return Resources.Load<Sprite>(path);
    }

    public Texture2D GetPortraitTexture(string name)
    {
        string path = "portraits/" + name;
        Object thing = Resources.Load(path);
        if (thing == null)
        {
            Debug.LogWarning(path + " not found");
        }
        return Resources.Load<Texture2D>(path);
    }

    public Texture2D GetHexPortraitTexture(string name)
    {
        string path = "hexportraits/" + name;
        Object thing = Resources.Load(path);
        if (thing == null)
        {
            Debug.LogWarning(path + " not found");
        }
        return Resources.Load<Texture2D>(path);
    }

    public GameObject GetGameEvent(string name)
    {
        string path = "gameevents/" + name;
        Object thing = Resources.Load(path);
        if (thing == null)
        {
            Debug.LogWarning(path + " not found");
        }
        return Resources.Load<GameObject>(path);
    }

    public GameObject GetStarSystem(string id)
    {
        string path = "starsystems/" + id;
        Object thing = Resources.Load(path);
        if (thing == null)
        {
            Debug.LogWarning(path + " not found");
        }
        return Resources.Load<GameObject>(path);
    }

    public GameObject GetUnitPrefab(string id)
    {
        string path = "units/" + id;
        Object thing = Resources.Load(path);
        if (thing == null)
        {
            Debug.LogWarning(path + " not found");
        }
        return Resources.Load<GameObject>(path);
    }

}
