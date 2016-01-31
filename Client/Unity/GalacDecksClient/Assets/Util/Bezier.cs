using UnityEngine;
using System.Collections;

/// <summary>
/// Utility class to calculate Bezier curves.
/// </summary>
public class Bezier  {

    public static Vector3 CalculatePoint(Vector3 p0, Vector3 p1, Vector3 p2, Vector3 p3, float t)
    {
        float u = 1 - t;
        float tt = t * t;
        float uu = u * u;
        float uuu = uu * u;
        float ttt = tt * t;

        Vector3 p = uuu * p0; 
        p += 3 * uu * t * p1; 
        p += 3 * u * tt * p2; 
        p += ttt * p3; 

        return p;
    }

    /// <summary>
    /// Returns the approximate length of the bezier curve, by summing the linear length of its segments
    /// </summary>
    /// <param name="p0"></param>
    /// <param name="p1"></param>
    /// <param name="p2"></param>
    /// <param name="p3"></param>
    /// <param name="segments"></param>
    /// <returns></returns>
    public static float Length(Vector3 p0, Vector3 p1, Vector3 p2, Vector3 p3, int segments = 4)
    {
        float length = 0;
        Vector3 last = p0;
        for(int i = 0; i < segments - 1; i++)
        {
            float t = ((float) (i + 1)) / (float) segments;
            Vector3 pos = CalculatePoint(p0, p1, p2, p3, t);
            length += Vector3.Distance(last, pos);
            last = pos;
        }
        return length;
    }
}
