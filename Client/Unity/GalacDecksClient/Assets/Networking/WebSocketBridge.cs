using UnityEngine;
using System.Collections;
using System.Collections.Generic;
# if UNITY_WEBGL || UNITY_WEBPLAYER
# else
using WebSocketSharp;
# endif

/// <summary>
/// Cross-platform Websocket class, using Javascript for browser and
/// WebSocketSharp elsewhere. Handles queueing inbound and outbound
/// messages for processing during Update() to avoid issues of 
/// executing outside of Unity's main thread.
/// </summary>
public class WebSocketBridge : MonoBehaviour
{
    public bool debugMode;

    public delegate void OnConnect();
    public delegate void OnClose();
    public delegate void OnTextMessage(string message);
    public delegate void OnError(string error);

    public OnConnect connectHandler;
    public OnClose closeHandler;
    public OnTextMessage onTextMessage;
    public OnError errorHandler;
    public float simulatedLatency = 0;

    private string host;
    private bool connected = false;
    // We track this so we can notify delegates during the Update()
    private bool connectedLastUpdate = false;

    private Queue<string> inboundQueue = new Queue<string>();
    private Queue<string> outboundQueue = new Queue<string>();
    private Queue<string> errors = new Queue<string>();

    // We need separate code paths anywhere we talk to the socket for both
    // Websocket sharp and javascript external calls.
# if UNITY_WEBGL || UNITY_WEBPLAYER
    public void Connect(string host)
    {
        Application.ExternalCall("WebSocketConnect", host);
    }

    public void ExternalMessage(string message) {
        inboundQueue.Enqueue(message);
    }

    public void WriteToWire(string message) 
    {
        if (debugMode) Debug.Log("Sending: " + message);
        Application.ExternalCall("WebSocketSend", message);
    }

    public void Connected() {
        connected = true;
        if (debugMode)
        {
            Debug.Log("Connected.");
        }
    }

# else
    private WebSocket websocket;

    public void Connect(string host)
    {
        this.host = host;
        if(websocket != null)
        {
            Debug.LogWarning("Replacing existing websocket");
        }
        websocket = new WebSocket(host);
        websocket.Log.Output = this.LogDelegate;
        if (debugMode)
        {
            Debug.Log("Connecting to " + host);
            websocket.Log.Level = LogLevel.Trace;
        }
        websocket.OnOpen += this.Open;
        websocket.OnClose += this.Close;
        websocket.OnMessage += this.Message;
        websocket.OnError += this.Error;
        
        websocket.ConnectAsync();
    }

    private void WriteToWire(string message)
    {
        if (debugMode) Debug.Log("Sending: " + message);
        websocket.SendAsync(message, null);
    }

    private void Message(System.Object sender, WebSocketSharp.MessageEventArgs e)
    {
        if (e.Type == Opcode.Text)
        {
            inboundQueue.Enqueue(e.Data);
        }
        else
        {
            Debug.Log("Unsupported type: " + e.Type);
        }
    }

    private void LogDelegate(LogData data, string str)
    {
        Debug.Log("WebSocket logged: " + data.Message);
    }

    private void Error(System.Object sender, WebSocketSharp.ErrorEventArgs e)
    {
        Debug.Log("Error: " + e.Message);
        errors.Enqueue(e.Message);
    }

    private void Open(System.Object sender, System.EventArgs e)
    {
        connected = true;
        if (debugMode)
        {
            Debug.Log("Connected.");
        }
    }

    private void Close(System.Object sender, System.EventArgs e)
    {
        connected = false;
        if (debugMode) Debug.Log("Connection closed.");
        if (closeHandler != null)
        {
            closeHandler();
        }
    }
#endif

    public bool IsConnected
    {
        get
        {
            return connected;
        }
    }

    void Update()
    {
        while (errors.Count > 0)
        {
            string error = errors.Dequeue();
            if (errorHandler != null)
            {
                errorHandler(error);
            }
            else
            {
                Debug.LogError("Error: " + error);
            }
        }
        while (outboundQueue.Count > 0)
        {
            string message = outboundQueue.Dequeue();
            if (simulatedLatency > 0)
            {
                StartCoroutine(SendDelayed(message, simulatedLatency));
            }
            else
            {
                WriteToWire(message);
            }
        }
        while (inboundQueue.Count > 0)
        {
            if (onTextMessage != null)
            {
                string message = inboundQueue.Dequeue();
                onTextMessage(message);
            }
            else
            {
                Debug.LogWarning("No onTextMessage handler");
            }
        }
        // Notify delegates of change in connection status:
        if (connected && !connectedLastUpdate)
        {
            if (connectHandler != null)
            {
                connectHandler();
            }
        }
        if (!connected && connectedLastUpdate)
        {
            if (closeHandler != null)
            {
                closeHandler();
            }
        }
        connectedLastUpdate = connected;
    }

    public void Send(string message)
    {
        outboundQueue.Enqueue(message);
    }

    private IEnumerator SendDelayed(string message, float delay)
    {
        yield return new WaitForSeconds(delay);
        WriteToWire(message);
    }




}
