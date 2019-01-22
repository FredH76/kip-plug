package com.crossAppStudio.backgroundVideo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.widget.Toast;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;


import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class BackgroundVideoPlugin extends CordovaPlugin {

  public static final int ERROR_CODE_NO_CAMERA = 1;
  public static final int ERROR_CODE_NO_PERMISSION_FOR_CAMERA = 2;
  public static final int ERROR_CODE_NO_PERMISSION_FOR_RECORD_AUDIO = 3;
  public static final int ERROR_CODE_NO_PERMISSION_FOR_WRITE_EXTERNAL_STORAGE = 4;


  public static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
  public static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 2;
  public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 3;

  private CallbackContext callbackContext;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    // save callbackContext
    this.callbackContext = callbackContext;

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
    // check if device has camera
    if (!cordova.getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
      callbackContext.error(ERROR_CODE_NO_CAMERA);
      return;
    }

    // ask for CAMERA PERMISSION
    if(!cordova.hasPermission(Manifest.permission.CAMERA)){
      cordova.requestPermission(this, MY_PERMISSIONS_REQUEST_CAMERA, Manifest.permission.CAMERA);
      return;
    }

    // ask for RECORD_AUDIO PERMISSION
    if(!cordova.hasPermission(Manifest.permission.RECORD_AUDIO)){
      cordova.requestPermission(this, MY_PERMISSIONS_REQUEST_RECORD_AUDIO, Manifest.permission.RECORD_AUDIO);
      return;
    }

    // ask for WRITE EXTERNAL PERMISSION
    if(!cordova.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
      cordova.requestPermission(this, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
      return;
    }

    /* ask for PERMISSION (native way, but issue with onRequestPermissionResult )
    if (ContextCompat.checkSelfPermission(cordova.getActivity().getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
      // Ask for permission
      ActivityCompat.requestPermissions(cordova.getActivity(),
        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
      return;
    }*/

    Intent intent = new Intent(cordova.getActivity(), BackgroundVideoService.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    cordova.getActivity().startService(intent);
    callbackContext.success("start camera"); // Thread-safe.
  }

  private void stopVideoRecord(CallbackContext callbackContext) {
    cordova.getActivity().stopService(new Intent(cordova.getActivity(), BackgroundVideoService.class));
    callbackContext.success("stop camera"); // Thread-safe.
  }

  @Override
  public void onRequestPermissionResult(int requestCode, String[] permission, int[] grantResults) throws JSONException
  {
    switch (requestCode) {
      case MY_PERMISSIONS_REQUEST_CAMERA: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          startVideoRecord(this.callbackContext);
        } else {
          Toast.makeText(cordova.getActivity().getBaseContext(), "Vous devez autoriser l'usage de la camera pour utiliser cette fonction", Toast.LENGTH_LONG).show();
          callbackContext.error(ERROR_CODE_NO_PERMISSION_FOR_CAMERA);
        }
        return;
      }
      case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          startVideoRecord(this.callbackContext);
        } else {
          Toast.makeText(cordova.getActivity().getBaseContext(), "Vous devez autoriser l'usage du micro pour utiliser cette fonction", Toast.LENGTH_LONG).show();
          callbackContext.error(ERROR_CODE_NO_PERMISSION_FOR_RECORD_AUDIO);
        }
        return;
      }
      case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          startVideoRecord(this.callbackContext);
        } else {
          Toast.makeText(cordova.getActivity().getBaseContext(), "Vous devez autoriser le stokage de fichier pour utiliser cette fonction", Toast.LENGTH_SHORT).show();
          callbackContext.error(ERROR_CODE_NO_PERMISSION_FOR_WRITE_EXTERNAL_STORAGE);
        }
        return;
      }
    }
  }
}
