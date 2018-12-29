package com.crossAppStudio.backgroundVideo;

import java.io.IOException;
import java.util.List;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class BackgroundVideoService extends Service {
    private static final String TAG = "BackgroundVideoService";
    private static Camera mServiceCamera;
    private boolean mRecordingStatus;
    private MediaRecorder mMediaRecorder;
    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

    @Override
    public void onCreate() {
        mRecordingStatus = false;
        // mServiceCamera = CameraRecorder.mCamera;
        // mServiceCamera = Camera.open(1);

        super.onCreate();
        if (mRecordingStatus == false)
            startRecording();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        stopRecording();
        mRecordingStatus = false;

        super.onDestroy();
    }

    public boolean startRecording() {
        Toast.makeText(getBaseContext(), "Recording Started", Toast.LENGTH_SHORT).show();
        toneGen1.startTone(ToneGenerator.TONE_SUP_CONGESTION);
        /*
         * try {
         *
         * // mServiceCamera = Camera.open(); Camera.Parameters params =
         * mServiceCamera.getParameters(); mServiceCamera.setParameters(params);
         * Camera.Parameters p = mServiceCamera.getParameters();
         *
         * final List<Size> listSize = p.getSupportedPreviewSizes(); Size mPreviewSize =
         * listSize.get(2); Log.v(TAG, "use: width = " + mPreviewSize.width +
         * " height = " + mPreviewSize.height); p.setPreviewSize(mPreviewSize.width,
         * mPreviewSize.height); p.setPreviewFormat(PixelFormat.YCbCr_420_SP);
         * mServiceCamera.setParameters(p);
         *
         * try { mServiceCamera.setPreviewDisplay(mSurfaceHolder);
         * mServiceCamera.startPreview(); } catch (IOException e) { Log.e(TAG,
         * e.getMessage()); e.printStackTrace(); }
         *
         * mServiceCamera.unlock();
         *
         * mMediaRecorder = new MediaRecorder();
         * mMediaRecorder.setCamera(mServiceCamera);
         * mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
         * mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
         * mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
         * mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
         * mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
         * mMediaRecorder.setOutputFile("/sdcard/video.mp4");
         * mMediaRecorder.setVideoFrameRate(30);
         * mMediaRecorder.setVideoSize(mPreviewSize.width, mPreviewSize.height);
         * //mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
         *
         * mMediaRecorder.prepare(); mMediaRecorder.start();
         *
         * mRecordingStatus = true;
         *
         * return true; } catch (IllegalStateException e) { Log.d(TAG, e.getMessage());
         * e.printStackTrace(); return false; } catch (IOException e) { Log.d(TAG,
         * e.getMessage()); e.printStackTrace(); return false; }
         */
        return true;
    }

    public void stopRecording() {
        Toast.makeText(getBaseContext(), "Recording Stopped", Toast.LENGTH_SHORT).show();
        toneGen1.stopTone();
        /*
         * try { mServiceCamera.reconnect(); } catch (IOException e) { // TODO
         * Auto-generated catch block e.printStackTrace(); } mMediaRecorder.stop();
         * mMediaRecorder.reset();
         *
         * mServiceCamera.stopPreview(); mMediaRecorder.release();
         *
         * mServiceCamera.release(); mServiceCamera = null;
         */
    }
}
