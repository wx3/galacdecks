using UnityEngine;
using System.Collections.Generic;

/// <summary>
/// A prefabstore contains a list of GameObject prefabs and allows retrieval 
/// of those prefabs based on their name in the Unity Editor. This is an alternative
/// to Resources.Load that restricts instantiation to specific options and ensures
/// they are in memory.
/// </summary>
public class PrefabStore : MonoBehaviour {

    [SerializeField]
    List<GameObject> prefabs;

    protected Dictionary<string, GameObject> lookup = new Dictionary<string, GameObject>();

    void Start()
    {
        foreach(GameObject prototype in prefabs)
        {
            if(prototype != null)
            {
                if(lookup.ContainsKey(prototype.name))
                {
                    Debug.LogWarning("Duplicate key '" + prototype.name + "' found in prototype store");
                }
                lookup[prototype.name] = prototype;
            }
        }
    }

    public GameObject GetPrefab(string name)
    {
        if(lookup.ContainsKey(name))
        {
            return lookup[name];
        }
        return null;
    }
    
}

