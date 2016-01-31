using UnityEngine;
using System.Collections;

public class Singleton<T> : MonoBehaviour where T : Component {

    private static T _instance;

    public bool dontDestroyOnLoad;

	public static T Instance
    {
        get
        {
            if(_instance == null)
            {
                var objs = FindObjectsOfType(typeof(T)) as T[];
                if (objs.Length != 1)
                {
                    Debug.LogError("Expected exactly one " + typeof(T).Name + " in scene, found " + objs.Length);
                }
                if (objs.Length > 0)
                {
                    _instance = objs[0];
                }
            }
            return _instance;
        }
    }

    protected virtual void Awake()
    {
        if(_instance != null)
        {
            Destroy(gameObject);
            Debug.LogWarning("Already an instance of " + this + ", self-destructing");
        }
        else
        {
            if(dontDestroyOnLoad)
            {
                DontDestroyOnLoad(gameObject);
            }
        }
    }
}
