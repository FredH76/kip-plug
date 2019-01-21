package com.crossAppStudio.backgroundVideo;

import android.content.pm.PackageManager;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.widget.Toast;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class BackgroundVideoPlugin extends CordovaPlugin {

    public static final int ERROR_CODE_NO_CAMERA = 1;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        }
        if (action.equals("hasCamera")) {
            this.hasCamera(callbackContext);
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
      // do something cool here
    }


    private void hasCamera(CallbackContext callbackContext) {
        if (cordova.getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(cordova.getActivity().getBaseContext(), "this device has camera", Toast.LENGTH_SHORT).show();
            callbackContext.success();
        } else {
            Toast.makeText(cordova.getActivity().getBaseContext(), "this device has no camera", Toast.LENGTH_SHORT).show();
            callbackContext.error(ERROR_CODE_NO_CAMERA);
        }
    }

    private void startVideoRecord(CallbackContext callbackContext) {
        //test if device is equiped with camera
        if (!cordova.getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            callbackContext.error(ERROR_CODE_NO_CAMERA);
            return;
         }

        Intent intent = new Intent(cordova.getActivity(), BackgroundVideoService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        cordova.getActivity().startService(intent);
        callbackContext.success("start camera"); // Thread-safe.
    }

    private void stopVideoRecord(CallbackContext callbackContext) {
         //test if device is equiped with camera
         if (!cordova.getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            callbackContext.error(ERROR_CODE_NO_CAMERA);
            return;
         }

        cordova.getActivity().stopService(new Intent(cordova.getActivity(), BackgroundVideoService.class));
        callbackContext.success("stop camera"); // Thread-safe.
    }

}
