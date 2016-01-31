using UnityEngine;
using System;
using System.Collections;

public class ApplicationError : EventArgs {

    private string errorMessage;

    public string Message
    {
        get
        {
            return errorMessage;
        }
    }

    public ApplicationError(string message)
    {
        this.errorMessage = message;
    }
}
