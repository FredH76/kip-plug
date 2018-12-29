package com.crossAppStudio.backgroundVideo;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class BackgroundVideoPlugin extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        }
        if (action.equals("startVideoRecord")) {
            this.startVideoRecord(callbackContext);
            return true;
        }
        if (action.equals("stopVideoRecord")) {
            this.stopVideoRecord(callbackContext);
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {

        Intent intent = new Intent(cordova.getActivity(), BackgroundVideoService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        cordova.getActivity().startService(intent);
        //cordova.getActivity().finish();
        callbackContext.success("start camera"); // Thread-safe.
    }

    private void startVideoRecord(CallbackContext callbackContext) {
        Intent intent = new Intent(cordova.getActivity(), BackgroundVideoService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        cordova.getActivity().startService(intent);
        callbackContext.success("start camera"); // Thread-safe.
    }

    private void stopVideoRecord(CallbackContext callbackContext) {
        cordova.getActivity().stopService(new Intent(cordova.getActivity(), BackgroundVideoService.class));
        callbackContext.success("stop camera"); // Thread-safe.
    }

}
