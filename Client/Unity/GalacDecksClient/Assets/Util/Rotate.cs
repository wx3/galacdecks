using UnityEngine;
using System.Collections;

/**
 * Simple script to rotate an object on an axis.
 **/
public class Rotate : MonoBehaviour
{

    // Axis of rotation:
    public Vector3 axis = Vector3.up;
    public bool randomAxis = false;

    // How many seconds to a complete revolution?
    public float period = 1;
    public bool randomPeriod = false;
    public float minPeriod, maxPeriod;

    public bool startRandom = false;

    void Start()
    {
        if (period < 0.01f)
        {
            period = 0.01f;
        }
        if(randomAxis)
        {
            axis = Random.insideUnitSphere;
        }
        if(randomPeriod)
        {
            period = Random.Range(minPeriod, maxPeriod);
        }
        if (axis.magnitude == 0)
        {
            axis = Vector3.up;
        }
        if (axis.magnitude != 1)
        {
            axis = axis.normalized;
        }
        if (startRandom) transform.localRotation = Random.rotation;
    }

    // Update is called once per frame
    void Update()
    {
        transform.Rotate(axis * Time.deltaTime * (360 / period));
    }
}
