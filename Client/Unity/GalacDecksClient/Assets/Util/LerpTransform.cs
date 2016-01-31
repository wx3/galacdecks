using UnityEngine;
using System.Collections;

/// <summary>
/// Smoothly interpolates position, scale and rotation of a transform, either together 
/// or independently.
/// </summary>
public class LerpTransform : MonoBehaviour { 

    private Vector3 startPosition;
    private Vector3 targetPosition;
    private float positionTimer;
    private float positionDuration;

    private Vector3 startScale;
    private Vector3 targetScale;
    private float scaleTimer;
    private float scaleDuration;

    private Quaternion startRotation;
    private Quaternion targetRotation;
    private float rotationTimer;
    private float rotationDuration;

    public AnimationCurve curve;
   
    /// <summary>
    /// Set the position, scale and rotation targets to those of the supplied transform.
    /// </summary>
    /// <param name="transform"></param>
    /// <param name="duration"></param>
    public void SetTransform(Transform transform, float duration)
    {
        SetPosition(transform.position, duration);
        SetScale(transform.localScale, duration);
        SetRotation(transform.rotation, duration);
    }

    public void SetPosition(Vector3 position, float duration = 0)
    {
        if(position != targetPosition || duration != positionDuration)
        {
            startPosition = transform.position;
            targetPosition = position;
            positionTimer = 0;
            positionDuration = duration;
        }
    }

    public void SetScale(Vector3 scale, float duration = 0)
    {
        if(scale != targetScale || duration != scaleDuration)
        {
            startScale = transform.localScale;
            targetScale = scale;
            scaleTimer = 0;
            scaleDuration = duration;
        }
    }

    public void SetRotation(Quaternion rotation, float duration = 0)
    {
        if (rotation != targetRotation || duration != rotationDuration)
        {
            startRotation = transform.rotation;
            targetRotation = rotation;
            rotationTimer = 0;
            rotationDuration = duration;
        }
    }

    void Update()
    {
        positionTimer += Time.deltaTime;
        scaleTimer += Time.deltaTime;
        rotationTimer += Time.deltaTime;
        float amount = 1;
        if(positionDuration > 0)
        {
            amount = curve.Evaluate((positionTimer * curve.length) / positionDuration);
            transform.position = Vector3.Lerp(startPosition, targetPosition, amount);
        }
        amount = 1;
        if(scaleDuration > 0)
        {
            amount = curve.Evaluate((scaleTimer * curve.length) / scaleDuration);
            transform.localScale = Vector3.Lerp(startScale, targetScale, amount);
        }
        amount = 1;
        if(rotationDuration > 0)
        {
            amount = curve.Evaluate((rotationTimer * curve.length) / rotationDuration);
            transform.rotation = Quaternion.Lerp(startRotation, targetRotation, amount);
        }
    }

}
