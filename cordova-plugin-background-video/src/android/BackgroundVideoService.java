package com.crossAppStudio.backgroundVideo;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Notification;
import android.app.PendingIntent;
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

import io.ionic.starter.R;

public class BackgroundVideoService extends Service {
  public static final int QUALITY_MIN = 0;
  public static final int QUALITY_LOW = 1;
  public static final int QUALITY_MEDIUM = 2;
  public static final int QUALITY_HIGH = 3;

  public static final int CAMERA_POSITION_BACK = 0;
  public static final int CAMERA_POSITION_FRONT = 1;

  private static final String TAG = "BackgroundVideoService";
  private static Camera mCamera;
  //private ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
  private boolean mRecordingStatus;
  private MediaRecorder mMediaRecorder;
  private int videoResolution= CamcorderProfile.QUALITY_LOW;
  private int camNumber = 0;
  private int camOrientation = 90;
  private String fileDestination;

  @Override
  public void onStart(Intent intent, int startId) {

    // return if already recording in progress
    if (mRecordingStatus == true) {
      Toast.makeText(getBaseContext(), "Recording already in progress", Toast.LENGTH_SHORT).show();
      return;
    }
    // get service parameter (CAM POSITION, RESOLUTION, FILE DESTINATION, ...)
    //camPosition = intent.getParcelableExtra("camPosisiton");
    fileDestination = intent.getStringExtra("fileDestination");
    int quality = intent.getIntExtra("quality",0);
    int cameraSelection = intent.getIntExtra("cameraSelection",0);

    //set resolution
    setResolution(quality);

    //select Camera
    selectCamera(cameraSelection);

    // start recording
    startRecording();

    // Put this service in foreground to prevent being killed by system
    // rq: notification is mandatory for Android 9 (API >= 28)
    Intent notificationIntent = new Intent();
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
    Notification notification = new Notification.Builder(this)
      .build();
    startForeground(1, notification);

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
   * setResolution : set video recording resolution
   ************************************************************************************************/
  private void setResolution(int quality) {
    switch (quality) {
      case QUALITY_MIN: //0
        this.videoResolution = CamcorderProfile.QUALITY_QVGA; //240x320
        break;
      case QUALITY_LOW: //1
        this.videoResolution = CamcorderProfile.QUALITY_480P; //480x720
        break;
      case QUALITY_MEDIUM: //2
        this.videoResolution = CamcorderProfile.QUALITY_720P; //720x1280
        break;
      case QUALITY_HIGH: //3
        this.videoResolution = CamcorderProfile.QUALITY_1080P; //1088x1920
        break;
      default:
        this.videoResolution = CamcorderProfile.QUALITY_LOW;
    }
  }

  /*************************************************************************************************
   * select Camera : BACK or FRONT
   ************************************************************************************************/
  private void selectCamera(int pos){
    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
    int numberOfCameras = Camera.getNumberOfCameras();

    // scan all camera available
    for (int i = 0; i < numberOfCameras; i++) {
      Camera.getCameraInfo(i, cameraInfo);

      //select the first FRONT camera if user's choice
      if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        if(pos == CAMERA_POSITION_FRONT){
          this.camNumber=i;
          this.camOrientation = cameraInfo.orientation;
          return;
        }
      }
    }
    this.camNumber=0;
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
      mCamera = getCameraInstance(this.camNumber);
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
      mMediaRecorder.setOrientationHint(this.camOrientation);
      mMediaRecorder.setProfile(CamcorderProfile.get(this.videoResolution));

      // Step 4: Set output file
      mMediaRecorder.setOutputFile(fileDestination);

      // Step 5: Set hidden preview output (requires API Level 23 or higher)
      Surface hiddenSurface = MediaCodec.createPersistentInputSurface();
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
      } catch (IllegalStateException e) {
      }
      ;
      mMediaRecorder.reset();   // clear recorder configuration
      mMediaRecorder.release(); // release the recorder object
      mMediaRecorder = null;

    }

    // STOP and RELEASE CAMERA
    if (mCamera != null) {
      mCamera.lock();           // lock camera for later use
      mCamera.release();        // release the camera for other applications
      mCamera = null;
    }
  }

  /*************************************************************************************************
   * Get a Camera instance (FRONT or BACK)
   ************************************************************************************************/
  private static Camera getCameraInstance(int camNum) {
    Camera c = null;
    try {
      c = Camera.open(camNum); // attempt to get a Camera instance
    } catch (Exception e) {
      // Camera is not available (in use or does not exist)
    }
    return c;
  }
}
