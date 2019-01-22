package com.crossAppStudio.backgroundVideo;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class BackgroundVideoService extends Service {
  private static final String TAG = "BackgroundVideoService";
  private static Camera mCamera;
  //private ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
  private boolean mRecordingStatus;
  private MediaRecorder mMediaRecorder;
  private int videoResolution;
  private String fileDestination;
  private int camPosition;

  @Override
  public void onStart(Intent intent, int startId) {

    // get service parameter (CAM POSITION, RESOLUTION, FILE DESTINATION, ...)
    //camPosition = intent.getParcelableExtra("camPosisiton");
    //fileDestination = intent.getParcelableExtra("fileDestination");

    // start recording if not yet in progress
    if (mRecordingStatus == false)
      startRecording();
    super.onStart(intent, startId);
  }

  @Override
  public void onCreate() {
    // initialize recording status
    mRecordingStatus = false;
    super.onCreate();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onDestroy() {
    // stop recording
    stopRecording();
    super.onDestroy();
  }

  /*************************************************************************************************
   * startRecording : prepare media recorder and start video recording 
   ************************************************************************************************/
  private boolean startRecording() {
    mRecordingStatus = true;
    Toast.makeText(getBaseContext(), "Recording Started", Toast.LENGTH_SHORT).show();

    //START TONE
    //toneGen1.startTone(ToneGenerator.TONE_SUP_CONGESTION);

    // START VIDEO
    try {
      // STEP 0 : Init CAMERA and MEDIA RECORDER
      mCamera = getCameraInstance();
      if (mCamera == null)
        stopRecording();
      mMediaRecorder = new MediaRecorder();

      // Step 1: Unlock and set camera to MediaRecorder
      mCamera.unlock();
      mMediaRecorder.setCamera(mCamera);

      // Step 2: Set sources
      mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
      mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
      //mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

      // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
      mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

      // Step 4: Set output file
      mMediaRecorder.setOutputFile(getOutputMediaFile(null).toString());

      // Step 5: Set hidden preview output (requires API Level 23 or higher)
      Surface hiddenSurface= MediaCodec.createPersistentInputSurface();
      //mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
      mMediaRecorder.setPreviewDisplay(hiddenSurface);

      // Step 6: Prepare and start MediaRecorder
      mMediaRecorder.prepare();
      mMediaRecorder.start();

      return true;
    } catch (IllegalStateException e) {
      Log.d(TAG, e.getMessage());
      e.printStackTrace();
      stopRecording();
      return false;
    } catch (IOException e) {
      Log.d(TAG, e.getMessage());
      e.printStackTrace();
      stopRecording();
      return false;
    }
  }

  /*************************************************************************************************
   * stopRecording : stop video recording and free all resources
   ************************************************************************************************/
  private void stopRecording() {
    mRecordingStatus = false;
    Toast.makeText(getBaseContext(), "Recording Stopped", Toast.LENGTH_SHORT).show();

    // STOP TONE GENERATOR
    // toneGen1.stopTone();

    // STOP AND RELEASE MEDIA RECORDER
    if (mMediaRecorder != null) {
      mMediaRecorder.stop();  // stop the recording
      mMediaRecorder.reset();   // clear recorder configuration
      mMediaRecorder.release(); // release the recorder object
      mMediaRecorder = null;
    }

    // STOP and RELEASE CAMERA
    if (mCamera != null){
      mCamera.lock();           // lock camera for later use
      mCamera.release();        // release the camera for other applications
      mCamera = null;
    }
  }

  /*************************************************************************************************
   * Get a Camera instance (FRONT or BACK)
   ************************************************************************************************/
  private static Camera getCameraInstance() {
    Camera c = null;
    try {
      c = Camera.open(); // attempt to get a Camera instance
    } catch (Exception e) {
      // Camera is not available (in use or does not exist)
    }
    return c; 
  }


  /*************************************************************************************************
   * Create a File for saving a video
   ************************************************************************************************/
  private static File getOutputMediaFile(String fileDest) {
    File mediaFile;

    // To be safe, you should check that the SDCard is mounted
    // using Environment.getExternalStorageState() before doing this.

    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES), "MyCameraApp");
    // This location works best if you want the created images to be shared
    // between applications and persist after your app has been uninstalled.

    // Create the storage directory if it does not exist
    if (!mediaStorageDir.exists()) {
      if (!mediaStorageDir.mkdirs()) {
        Log.d("MyCameraApp", "failed to create directory");
        return null;
      }
    }

    // Create a media file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    mediaFile = new File(mediaStorageDir.getPath() + File.separator +
        "VID_" + timeStamp + ".mp4");

    return mediaFile;
  }

}
