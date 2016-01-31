using System.Collections.Generic;

[System.Serializable]
public class PlayerView  {

    public string playerName;
    public int position;
    public int homeworldId;
    public string shipPrefab;
    public int deckSize;
    public List<EntityView> hand;
    public Dictionary<string, int> currentResources;
    public Dictionary<string, int> maxResources;

    public int GetResource(string name)
    {
        if (currentResources.ContainsKey(name)) return currentResources[name];
        return 0;
    }

    public int GetMaxResource(string name)
    {
        if (maxResources.ContainsKey(name)) return maxResources[name];
        return 0;
    }
}
