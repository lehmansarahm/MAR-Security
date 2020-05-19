package edu.temple.mar_security.headless_tf;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.temple.mar_security.headless_tf.tflite.Classifier;

public class SingleClassifierActivity extends HeadlessVideoActivity {

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
    private static final String MODEL_FILENAME = "model.tflite";

    // TODO - update to reflect the name and extension of the TF labels file (stored in project "assets" folder)
    private static final String LABEL_FILENAME = "labels.txt";

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



    private int sensorOrientation;

    private long lastProcessingTimeMs;
    private long sysStartTime;

    private Classifier classifier;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorOrientation = (90 - getScreenOrientation());
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
            classifier = Classifier.create(this, MODEL_FILENAME, LABEL_FILENAME,
                    Classifier.Device.CPU, 1);
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
                            final List<Classifier.Recognition> results =
                                    classifier.recognizeImage(formattedBmp, sensorOrientation);
                            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                            Log.v(getLogTag(), "Found " + results.size()
                                    + " results in " + lastProcessingTimeMs + "ms");
                            for (Classifier.Recognition result : results) {
                                Log.v(getLogTag(), "\t\t Title: " + result.getTitle()
                                        + " \t\t Confidence: " + result.getConfidence());
                            }
                        }
                        processNextFrame();
                    });
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