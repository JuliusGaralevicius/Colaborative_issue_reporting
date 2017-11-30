package com.example.julius.colabmap;

import javax.security.auth.callback.Callback;

/**
 * Created by julius on 26/11/2017.
 */

public interface OnSendIssueCallback {
    public enum CallbackCode{
        SUCESS,
        CANCELED
    }
    public void OnSendIssue(CustomIssueMarkerData data, CallbackCode code);
}
