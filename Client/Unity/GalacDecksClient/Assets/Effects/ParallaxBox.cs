using UnityEngine;
using System.Collections;

public class ParallaxBox : MonoBehaviour
{

    // What are we following?
    public GameObject targetCamera;

    // What object is spawned as a particle?
    public GameObject prefab;

    // Number from 0-1 indicating the target density of particles, with 1 being maxCount
    public float density;

    // What is the maximum number of particles?
    public int maxCount;

    // A constant motion of the parallox items:
    public Vector3 wind;

    public Vector3 apparentSpeed;

    public float speedScale = 1;

    // If true, will scale the objects based on inverse depth, with the farthest distance being scale 1.
    public bool depthScale;

    // If true, will rotate the particle randomly
    public bool randomRotation;

    public float width = 100;
    public float height = 100;

    public float minDepth = 0;
    public float maxDepth = 100;

    public float shiftFactor = 1;

    public GameObject[] particles;

    private Camera _camera;
    private Vector3 _lastPosition;

    private Color32 _particleTint;

    void Start()
    {
        if (targetCamera == null)
        {
            targetCamera = Camera.main.gameObject;
            if (targetCamera == null)
            {
                enabled = false;
                return;
            }
        }
        _camera = targetCamera.GetComponent<Camera>();
        if (_camera == null)
        {
            Debug.Log("No camera");
            enabled = false;
            return;
        }
        _lastPosition = _camera.transform.position;
    }

    public void Init()
    {
        if (targetCamera == null)
        {
            Debug.Log("Cannot init parallax box without camera.");
            return;
        }
        particles = new GameObject[maxCount];
        int targetParticles = (int)((float)maxCount * density);
        int visible = 0;
        for (int i = 0; i < maxCount; i++)
        {
            // Distance from camera:
            float depth = Random.Range(minDepth, maxDepth);
            Vector3 pos = new Vector3(
                Random.Range(-width / 2, width / 2),
                -depth,
                Random.Range(-height / 2, height / 2)
            );

            GameObject particle = Instantiate(prefab) as GameObject;
            particle.transform.parent = gameObject.transform;
            particle.transform.localPosition = pos;
            particle.transform.localRotation = Quaternion.identity;

            if(depthScale)
            {
                float range = maxDepth - minDepth;
                float scale = (maxDepth - depth) / range;
                particle.transform.localScale = new Vector3(scale, scale, scale);
            }
            if(randomRotation)
            {
                particle.transform.localEulerAngles = new Vector3(0, Random.Range(0, 360), 0);
            }
            if (particle.activeSelf && visible >= targetParticles)
            {
                particle.SetActive(false);
            }
            if (!particle.activeSelf && visible < targetParticles)
            {
                particle.SetActive(true);
            }
            if(particle.activeSelf) ++visible;
            particles[i] = particle;
        }
    }


    // Update is called once per frame
    void Update()
    {
        
        if (particles.Length == 0)
        {
            Init();
        }
        if (_camera == null)
        {
            Debug.Log("Missing camera");
            return;
        }
        float dx = targetCamera.transform.position.x - _lastPosition.x;
        float dy = targetCamera.transform.position.y - _lastPosition.y;
        float dz = targetCamera.transform.position.z - _lastPosition.z;

        float left = -(width / 2);
        float right = (width / 2);
        float top = -(height / 2);
        float bottom = (height / 2);

        int targetParticles = (int)((float)maxCount * density);
        int visible = 0;
        for (int i = 0; i < particles.Length; i++)
        {
            GameObject particle = particles[i];

            Vector3 newPos = particle.transform.localPosition;

            // How far from the camera are we?
            float cameraDepth = Mathf.Abs(targetCamera.transform.position.y - particle.transform.position.y);

            if (cameraDepth < 0.001f) cameraDepth = 0.001f;

            // What is that expressed as an inverse ratio to the far clip plane?
            float depthRatio = 1;
            if(depthScale)
            {
                depthRatio = _camera.farClipPlane / cameraDepth;
            } 

            dx += (wind.x + (apparentSpeed.x * speedScale)) * Time.deltaTime;
            dy += (wind.y + (apparentSpeed.y * speedScale)) * Time.deltaTime;
            dz += (wind.z + (apparentSpeed.z * speedScale)) * Time.deltaTime;

            newPos.x -= dx * depthRatio * shiftFactor;
            newPos.y -= dy;
            newPos.z -= dz * depthRatio * shiftFactor;

            bool jumping = false;
            if (newPos.x < left)
            {
                newPos.x += width;
                jumping = true;
            }
            if (newPos.x > right)
            {
                newPos.x -= width;
                jumping = true;
            }
            if (newPos.z < top)
            {
                newPos.z += height;
                jumping = true;
            }
            if (newPos.z > bottom)
            {
                newPos.z -= height;
                jumping = true;
            }
            // If the particle has been jump shifted because it exited the box, we
            // destroy and create a new one to erase the particle trail. Probably
            // a more efficient way to do this but I don't know what it is:
            if (jumping)
            {
                Vector3 scale = particle.transform.localScale;
                Destroy(particle);
                particle = InstantiateParticle();
                particle.transform.localScale = scale;
                particle.transform.parent = gameObject.transform;
                particle.transform.localPosition = newPos;
                particle.transform.localRotation = Quaternion.identity;
                particles[i] = particle;
                if (particle.activeSelf && visible >= targetParticles)
                {
                    particle.SetActive(false);
                }
                if (!particle.activeSelf && visible < targetParticles)
                {
                    particle.SetActive(true);
                }
            }
            particle.transform.localPosition = newPos;

            if (particle.activeSelf) ++visible;
        }

        _lastPosition = targetCamera.transform.position;
    }

    protected virtual GameObject InstantiateParticle()
    {
        return Instantiate(prefab) as GameObject;
    }
}
