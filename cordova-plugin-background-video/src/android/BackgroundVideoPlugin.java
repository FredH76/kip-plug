package com.crossAppStudio.backgroundVideo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;


import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BackgroundVideoPlugin extends CordovaPlugin {

  public static final int ERROR_CODE_NO_CAMERA = 1;
  public static final int ERROR_CODE_NO_PERMISSION_FOR_CAMERA = 2;
  public static final int ERROR_CODE_NO_PERMISSION_FOR_RECORD_AUDIO = 3;
  public static final int ERROR_CODE_NO_PERMISSION_FOR_WRITE_EXTERNAL_STORAGE = 4;
  public static final int ERROR_CODE_PARAMETER_EXPECTED = 5;
  public static final int ERROR_CODE_INVALID_FILE_DESTINATION = 6;


  public static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
  public static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 2;
  public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 3;

  private static final String TAG = "BackgroundVideoService";
  private int videoQuality;
  private int cameraSelection;
  private String fileDestination = "";
  private CallbackContext callbackContext;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    // save callbackContext
    this.callbackContext = callbackContext;

    if (action.equals("hasCamera")) {
      this.hasCamera(callbackContext);
      return true;
    }

    if (action.equals("setQuality")) {

      // check if quality parameter is present
      if(!(args.get(0) instanceof Integer)){
        callbackContext.error(ERROR_CODE_PARAMETER_EXPECTED);
        return false;
      }

      // get quality from args[]
      int quality = args.getInt(0);

      // set quality for video
      this.setQuality(quality, callbackContext);
      return true;
    }

    if (action.equals("selectCamera")) {

      // check if cameraSelection parameter is present
      if(!(args.get(0) instanceof Integer)){
        callbackContext.error(ERROR_CODE_PARAMETER_EXPECTED);
        return false;
      }

      // get cameraSelection from args[]
      int cameraSelection = args.getInt(0);

      // select camera to use for video
      this.selectCamera(cameraSelection, callbackContext);
      return true;
    }

    if (action.equals("startVideoRecord")) {

      // check if fileDestination parameter is present
      if (args.getString(0) == "null") {
        callbackContext.error(ERROR_CODE_PARAMETER_EXPECTED);
        return false;
      }

      // get file destination from args[]
      this.fileDestination = args.getString(0);

      // remove cordova prefixe
      this.fileDestination= this.fileDestination.replaceAll("file://","");

      // test if file destiantion is valid
      if(!isFileDestinationValid(this.fileDestination)){
        callbackContext.error(ERROR_CODE_INVALID_FILE_DESTINATION);
        return false;
      }
      // this.fileDestination = createOutputMediaFile(); // FOR DEBUG ONLY

      // start video record
      this.startVideoRecord(this.fileDestination, callbackContext);
      return true;
    }

    if (action.equals("stopVideoRecord")) {
      this.stopVideoRecord(callbackContext);
      return true;
    }
    return false;
  }


  /*************************************************************************************************
   * hasCamera : test if device is equiped with camera
   ************************************************************************************************/
  private void hasCamera(CallbackContext callbackContext) {
    if (cordova.getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
      Toast.makeText(cordova.getActivity().getBaseContext(), "this device has camera", Toast.LENGTH_SHORT).show();
      callbackContext.success();
    } else {
      Toast.makeText(cordova.getActivity().getBaseContext(), "this device has no camera", Toast.LENGTH_SHORT).show();
      callbackContext.error(ERROR_CODE_NO_CAMERA);
    }
  }

  /*************************************************************************************************
   * setQuality : set video quality
   ************************************************************************************************/
  private void setQuality(int quality, CallbackContext callbackContext) {
    this.videoQuality = quality;
  }

  /*************************************************************************************************
   * setQuality : set video quality
   ************************************************************************************************/
  private void selectCamera(int cameraSelection, CallbackContext callbackContext) {
    //check if camera selection is available (FRONt or BACK)

    //else select defautl camera
    this.cameraSelection = cameraSelection;
  }
  /*************************************************************************************************
   * startVideoRecord : start recording a video and save it to destFile
   ************************************************************************************************/
  private void startVideoRecord(String fileDestination, CallbackContext callbackContext) {
    String fileDest  = fileDestination;

    // check if device has camera
    if (!cordova.getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
      callbackContext.error(ERROR_CODE_NO_CAMERA);
      return;
    }

    // ask for CAMERA PERMISSION
    if (!cordova.hasPermission(Manifest.permission.CAMERA)) {
      cordova.requestPermission(this, MY_PERMISSIONS_REQUEST_CAMERA, Manifest.permission.CAMERA);
      return;
    }

    // ask for RECORD_AUDIO PERMISSION
    if (!cordova.hasPermission(Manifest.permission.RECORD_AUDIO)) {
      cordova.requestPermission(this, MY_PERMISSIONS_REQUEST_RECORD_AUDIO, Manifest.permission.RECORD_AUDIO);
      return;
    }

    // ask for WRITE EXTERNAL PERMISSION
    if (!cordova.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
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
    intent.putExtra("quality", this.videoQuality);
    intent.putExtra("fileDestination", this.fileDestination);
    intent.putExtra("cameraSelection", this.cameraSelection);
    cordova.getActivity().startService(intent);
    callbackContext.success("start camera"); // Thread-safe.
  }

  /*************************************************************************************************
   * stopVideoRecord : stop recording a video
   ************************************************************************************************/
  private void stopVideoRecord(CallbackContext callbackContext) {
    cordova.getActivity().stopService(new Intent(cordova.getActivity(), BackgroundVideoService.class));
    callbackContext.success("stop camera"); // Thread-safe.
  }

  /*************************************************************************************************
   * onRequestPermissionResult : catch user permission choice
   ************************************************************************************************/
  @Override
  public void onRequestPermissionResult(int requestCode, String[] permission, int[] grantResults) throws JSONException {
    switch (requestCode) {
      case MY_PERMISSIONS_REQUEST_CAMERA: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          startVideoRecord(this.fileDestination, this.callbackContext);
        } else {
          Toast.makeText(cordova.getActivity().getBaseContext(), "Vous devez autoriser l'usage de la camera pour utiliser cette fonction", Toast.LENGTH_LONG).show();
          callbackContext.error(ERROR_CODE_NO_PERMISSION_FOR_CAMERA);
        }
        return;
      }
      case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          startVideoRecord(this.fileDestination, this.callbackContext);
        } else {
          Toast.makeText(cordova.getActivity().getBaseContext(), "Vous devez autoriser l'usage du micro pour utiliser cette fonction", Toast.LENGTH_LONG).show();
          callbackContext.error(ERROR_CODE_NO_PERMISSION_FOR_RECORD_AUDIO);
        }
        return;
      }
      case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          startVideoRecord(this.fileDestination, this.callbackContext);
        } else {
          Toast.makeText(cordova.getActivity().getBaseContext(), "Vous devez autoriser le stokage de fichier pour utiliser cette fonction", Toast.LENGTH_SHORT).show();
          callbackContext.error(ERROR_CODE_NO_PERMISSION_FOR_WRITE_EXTERNAL_STORAGE);
        }
        return;
      }
    }
  }

  /*************************************************************************************************
   * createOutputMediaFile : Create a File and Directory to save the video
   ************************************************************************************************/
  private String createOutputMediaFile() {

    // use the default movies directory of the app (to automatically delete video on app uninstall)
    File mediaStorageDir = cordova.getActivity().getBaseContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES);

    // Create the storage directory if it does not exist
    if (!mediaStorageDir.exists()) {
      if (!mediaStorageDir.mkdirs()) {
        Log.d("MyCameraApp", "failed to create directory");
        return null;
      }
    }

    // Create a unique file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String mediaFileName = mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4";

    return mediaFileName;
  }

  /*************************************************************************************************
   * isFileDestinationValid : test if filePath is valid or not
   ************************************************************************************************/
  private boolean isFileDestinationValid(String fileName) {

    try {
      File fileTest = new File(fileName);
      File dirTest = fileTest.getParentFile();

      // Create the storage directory if it does not exist
      if (!dirTest.exists()) {
        if (!dirTest.mkdirs()) {
          Log.d(TAG, "failed to create directory : " + dirTest.toString());
          return false;
        }
      }
      return true;
    } catch (NullPointerException e) {
      Log.d(TAG, "failed to create directory for file : " + fileName);
      return false;
    } catch (SecurityException e) {
      Log.d(TAG, "failed to create directory for file : " + fileName);
      return false;
    }
  }
}
