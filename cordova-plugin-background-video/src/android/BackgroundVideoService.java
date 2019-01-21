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
    public void onStart(Intent intent, int startId) {
        startRecording();

        super.onStart(intent, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopRecording();
        super.onDestroy();
    }

    public boolean startRecording() {
        Toast.makeText(getBaseContext(), "Recording Started", Toast.LENGTH_SHORT).show();
        toneGen1.startTone(ToneGenerator.TONE_SUP_CONGESTION);
        return true;
    }


    public void stopRecording() {
        Toast.makeText(getBaseContext(), "Recording Stopped", Toast.LENGTH_SHORT).show();
        toneGen1.stopTone();
    }
}
