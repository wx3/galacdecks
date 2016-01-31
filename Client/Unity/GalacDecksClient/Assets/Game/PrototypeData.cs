using System.Collections.Generic;

public class PrototypeData {

    public string id;
    public string name;
    public string description;
    public List<string> types;
    public string flavor;
    public string unitPrefab;
    public string summonEffect;
    public string projectile;
    public string portrait;

    public HashSet<string> tags;
    public Dictionary<string, int> stats;

    /// <summary>
    /// Unlocalized string of the types
    /// </summary>
    public string TypeString
    {
        get
        {
            List<string> types = new List<string>();
            if (tags.Contains("PLANET"))
            {
                types.Add("Planet");
            }
            if (tags.Contains("SHIP"))
            {
                types.Add("Ship");
            }
            if (tags.Contains("PHENOMENON"))
            {
                types.Add("Phenomenon");
            }
            if (tags.Contains("POWER"))
            {
                types.Add("Power");
            }
            return string.Join(" / ", types.ToArray());
        }
    }

    public int GetStat(string stat)
    {
        if(stats.ContainsKey(stat))
        {
            return stats[stat];
        }
        return 0;
    }



}
