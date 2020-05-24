package edu.temple.edge_playground;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.temple.edge_playground.res_lib.ref.HeadlessVideoActivity;
import edu.temple.edge_playground.tflite.Recognition;
import edu.temple.edge_playground.tflite.ObjectDetector;

public class MainActivity extends HeadlessVideoActivity {

    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------
    //
    //        EDIT THESE PROPERTIES !!
    //
    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------


    // TODO - update the log tag to something appropriate to what you're testing
    private static final String LOG_TAG = "Headless_TF";

    // TODO - update to reflect the name and extension of the TF model file (stored in project "assets" folder)
    private static final String MODEL_FILENAME = "detect.tflite";

    // TODO - update to reflect the name and extension of the TF labels file (stored in project "assets" folder)
    private static final String LABEL_FILENAME = "labelmap.txt";

    // TODO - update the video name to whatever you're using
    private static final String VIDEO_NAME = "long_video.mp4";


    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------





    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------
    //
    //        LEAVE THE REST OF THE CLASS ALONE
    //
    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------



    private static final int DEFAULT_INPUT_SIZE = 300;
    private static final boolean DEFAULT_IS_QUANTIZED = true;

    private long lastProcessingTimeMs;
    private long sysStartTime;

    private ObjectDetector classifier;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // MUST POPULATE THESE FIELDS ** BEFORE ** CALLING SUPER ON-CREATE
        setContentView(R.layout.activity_main);
        simpleVideoView = findViewById(R.id.simpleVideoView);

        super.onCreate(savedInstanceState);
        Log.d(getLogTag(), "onCreate");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(getLogTag(), "onResume");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (classifier != null) {
            classifier.close();
            classifier = null;
        }
    }

    @Override
    public String getLogTag() {
        return LOG_TAG;
    }

    @Override
    public String getVideoPath() {
        File moviesDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        File video = new File(moviesDir, VIDEO_NAME);
        return video.getAbsolutePath();
    }

    @Override
    protected void startCollecting() {
        try {
            classifier = ObjectDetector.create(getAssets(), MODEL_FILENAME, LABEL_FILENAME,
                    DEFAULT_INPUT_SIZE, DEFAULT_IS_QUANTIZED);
        } catch (IOException ex) {
            Log.e(getLogTag(), "Something went wrong while trying to instantiate the classifier!", ex);
            classifier = null;
        }

        sysStartTime = System.currentTimeMillis();
        processNextFrame();
    }

    // ---------------------------------------------------------------------------

    private void processNextFrame() {
        long currentTime = System.currentTimeMillis();
        long frameTimeMicros = ((currentTime - sysStartTime) * 1000);
        Log.i(getLogTag(), "Sending frame to video source with timestamp: " + frameTimeMicros);

        Bitmap frameBmp = retriever.getFrameAtTime(frameTimeMicros);
        if (frameBmp != null) {
            final Bitmap formattedBmp = frameBmp.copy(Bitmap.Config.ARGB_8888, true);
            runInBackground(
                    () -> {
                        if (classifier != null) {
                            final long startTime = SystemClock.uptimeMillis();
                            final List<Recognition> results = classifier.recognizeImage(formattedBmp);
                            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                            Log.v(getLogTag(), "Found " + results.size()
                                    + " results in " + lastProcessingTimeMs + "ms");
                            for (Recognition result : results) {
                                Log.v(getLogTag(), "\t\t Title: " + result.getTitle()
                                        + " \t\t Confidence: " + result.getConfidence());
                            }
                        }
                        processNextFrame();
                    });
        }
    }


}