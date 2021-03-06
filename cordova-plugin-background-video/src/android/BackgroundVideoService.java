package com.crossAppStudio.backgroundVideo;

import java.io.IOException;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

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
  private int videoResolution = CamcorderProfile.QUALITY_LOW;
  private int camNumber = 0;
  private int camOrientation = 90;
  private String fileDestination;

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

  @Override
  public void onStart(Intent intent, int startId) {

    // return if already recording in progress
    if (mRecordingStatus == true) {
      Toast.makeText(getBaseContext(), "Recording already in progress", Toast.LENGTH_SHORT).show();
      sendMessage("success", "Recording already in progress");
      return;
    }

    try {
      // get service parameter (CAM POSITION, RESOLUTION, FILE DESTINATION, ...)
      fileDestination = intent.getStringExtra("fileDestination");
      int quality = intent.getIntExtra("quality", 0);
      int cameraSelection = intent.getIntExtra("cameraSelection", 0);

      //set resolution
      setResolution(quality);

      //select Camera
      selectCamera(cameraSelection);

      // start recording
      startRecording();

      // Put this service in foreground to prevent being killed by system
      // rq: notification is mandatory for Android 9 (API >= 28)
      Intent notificationIntent = new Intent();
      //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
      Notification notification = new Notification.Builder(this)
        .setContentTitle("Video")
        //.setContentText("Enregistrement en cours ...")
        //.setSmallIcon(R.drawable.ic_camera_on)
        //.setContentIntent(pendingIntent)
        .build();
      startForeground(1, notification);
    } catch (Exception e) {
      try {
        stopRecording();
      } finally {
        sendMessage("error", "onStart() ERROR. " + e.getStackTrace());
      }
    }

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
    try {
      stopRecording();
    } catch (Exception e) {
      sendMessage("error", "onStop() ERROR. " + e.getStackTrace());
    }
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
  private void selectCamera(int pos) {
    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
    int numberOfCameras = Camera.getNumberOfCameras();

    // scan all camera available
    for (int i = 0; i < numberOfCameras; i++) {
      Camera.getCameraInfo(i, cameraInfo);

      //select the first FRONT camera if user's choice
      if ((pos == CAMERA_POSITION_FRONT) && (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)) {
          this.camNumber = i;
          this.camOrientation = cameraInfo.orientation;
          return;
      }
      //select the first BACK camera if user's choice
      if ((pos == CAMERA_POSITION_BACK) && (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK)) {
        this.camNumber = i;
        this.camOrientation = cameraInfo.orientation;
        return;
      }
    }

    // default camera selection:
    this.camNumber = 0;
  }

  /*************************************************************************************************
   * startRecording : prepare media recorder and start video recording
   ************************************************************************************************/
  private boolean startRecording() throws Exception {
    //START TONE
    //toneGen1.startTone(ToneGenerator.TONE_SUP_CONGESTION);

    // START VIDEO
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
    Surface mySurface = new Surface(new SurfaceTexture(1));
    //Surface hiddenSurface = MediaCodec.createPersistentInputSurface();
    //mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
    mMediaRecorder.setPreviewDisplay(mySurface);

    // Step 6: Prepare and start MediaRecorder
    mMediaRecorder.prepare();

    mMediaRecorder.start();

    mRecordingStatus = true;
    Toast.makeText(getBaseContext(), "Recording Started", Toast.LENGTH_SHORT).show();

    // call success callback
    sendMessage("success", "video started");

    return true;
  }

  /*************************************************************************************************
   * stopRecording : stop video recording and free all resources
   ************************************************************************************************/
  private void stopRecording() {
    Toast.makeText(getBaseContext(), "Recording Stopped", Toast.LENGTH_SHORT).show();

    // STOP TONE GENERATOR
    // toneGen1.stopTone();

    // STOP AND RELEASE MEDIA RECORDER
    if (mMediaRecorder != null) {
      try {
        mMediaRecorder.stop();  // stop the recording
      } catch (IllegalStateException e) {
        //sendMessage("error", "mMediaRecorder.stop()is called before start() in BackgroundVideoService");
      }
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

    mRecordingStatus = false;

    // call success callback
    sendMessage("success", "video stoped");
  }

  /*************************************************************************************************
   * send Error message to Activity
   ************************************************************************************************/
  private void sendMessage(String type, String msg) {
    Intent intent = new Intent("MessageFromService");
    intent.putExtra("type", type);
    intent.putExtra("message", msg);
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
  }
}
