package edu.temple.mar_security.headless_tf;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;

import edu.temple.mar_security.res_lib.BaseActivity;

public class MainActivity extends BaseActivity { // implements ProcessorListener {


    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------
    //
    //        EDIT THESE PROPERTIES !!
    //
    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------




    public static final String TAG = "Headless_TF";

    public static final String VIDEO_NAME = "long_video.mp4";
    // public static final String VIDEO_NAME = "coffee.mp4";

    // private BarcodeProcessor processor;


    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------




    private VideoView simpleVideoView;
    private MediaController mediaControls;
    private MediaMetadataRetriever retriever;

    private String videoPath;

    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File moviesDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        File video = new File(moviesDir, VIDEO_NAME);
        videoPath = video.getAbsolutePath();

        retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);

        // processor = new BarcodeProcessor(this);

        if (arePermissionsGranted()) moveForward();
        else getRuntimePermissions();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /* if (processor != null) {
            processor.stop();
        } */
    }

    @Override
    protected void moveForward() {
        initVideoView();
        startTime = System.currentTimeMillis();
        sendFrameToProcessor();
    }

    /* @Override
    public void onResultsAvailable() {
        Log.i(TAG, "Results received!  Sending next frame");
        sendFrameToProcessor();
    } */

    private void initVideoView() {
        simpleVideoView = findViewById(R.id.simpleVideoView);

        if (mediaControls == null) {
            mediaControls = new MediaController(MainActivity.this);
            mediaControls.setAnchorView(simpleVideoView);
        }

        simpleVideoView.setMediaController(mediaControls);
        simpleVideoView.setVideoPath(videoPath);
        simpleVideoView.start();

        simpleVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        simpleVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                MainActivity.this.finishAndRemoveTask();
            }
        });

        simpleVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                // display a toast when an error is occurred while playing an video
                Toast.makeText(getApplicationContext(),
                        "Oops An Error Occur While Playing Video...!!!",
                        Toast.LENGTH_LONG).show();
                return false;
            }
        });
    }

    private void sendFrameToProcessor() {
        long currentTime = System.currentTimeMillis();
        long frameTimeMicros = ((currentTime - startTime) * 1000);
        Log.i(TAG, "Sending frame to video source with timestamp: " + frameTimeMicros);

        Bitmap frameBmp = retriever.getFrameAtTime(frameTimeMicros);
        if (frameBmp != null) {
            // processor.process(frameBmp);
        }
    }

}