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
    fileDestination = intent.getStringExtra("fileDestination");

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

      // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
      mMediaRecorder.setOrientationHint(90);
      mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

      // Step 4: Set output file
      mMediaRecorder.setOutputFile(fileDestination);

      // Step 5: Set hidden preview output (requires API Level 23 or higher)
      Surface hiddenSurface= MediaCodec.createPersistentInputSurface();
      //mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
      mMediaRecorder.setPreviewDisplay(hiddenSurface);

      // Step 6: Prepare and start MediaRecorder
      mMediaRecorder.prepare();
      mMediaRecorder.start();

      return true;
    } catch (IllegalStateException e) {
      stopRecording();
      return false;
    } catch (IOException e) {
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
      try {
        mMediaRecorder.stop();  // stop the recording
      }
      catch(IllegalStateException e){};
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
}
