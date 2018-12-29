package com.crossAppStudio.backgroundVideo;

import android.media.AudioManager;
import android.media.ToneGenerator;

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
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        // define a runnable object
        //private runBeep =

        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
              ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
              while(true){
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,150);
                    try {
                      Thread.sleep(500);                    }
                      catch (InterruptedException e)  {
                    }
              }
            }
        });
        callbackContext.success("start beep loop"); // Thread-safe.
    }
}
