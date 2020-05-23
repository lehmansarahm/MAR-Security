package edu.temple.edge_playground.ref;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

public abstract class HeadlessVideoActivity extends BaseActivity {

    protected abstract String getLogTag();
    protected abstract String getVideoPath();
    protected abstract void startCollecting();

    // --------------------------------------------------------------

    protected VideoView simpleVideoView;
    protected MediaController mediaControls;
    protected MediaMetadataRetriever retriever;

    protected int previewWidth = 0, previewHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        retriever = new MediaMetadataRetriever();
        retriever.setDataSource(getVideoPath());

        Bitmap frameBmp = retriever.getFrameAtTime(0);
        previewWidth = frameBmp.getWidth();
        previewHeight = frameBmp.getHeight();

        if (arePermissionsGranted()) moveForward();
        else getRuntimePermissions();
    }

    @Override
    protected void moveForward() {
        initVideoView();
    }

    private void initVideoView() {
        if (mediaControls == null) {
            mediaControls = new MediaController(this);
            mediaControls.setAnchorView(simpleVideoView);
        }

        simpleVideoView.setMediaController(mediaControls);
        simpleVideoView.setVideoPath(getVideoPath());
        simpleVideoView.start();

        // simpleVideoView.setOnPreparedListener(mp -> mp.setLooping(true));

        simpleVideoView.setOnCompletionListener(mp ->
                HeadlessVideoActivity.this.finishAndRemoveTask());

        simpleVideoView.setOnErrorListener((mp, what, extra) -> {
            // display a toast when an error is occurred while playing an video
            Toast.makeText(getApplicationContext(),
                    "Oops An Error Occur While Playing Video...!!!",
                    Toast.LENGTH_LONG).show();
            return false;
        });
    }

    // --------------------------------------------------------------

    private Handler handler;
    private HandlerThread handlerThread;

    @Override
    public synchronized void onResume() {
        Log.d(getLogTag(), "onResume " + this);
        super.onResume();

        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        startCollecting();
    }

    @Override
    public synchronized void onPause() {
        Log.d(getLogTag(), "onPause " + this);

        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
            Log.e(getLogTag(), "Exception!", e);
        }

        super.onPause();
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }

}
