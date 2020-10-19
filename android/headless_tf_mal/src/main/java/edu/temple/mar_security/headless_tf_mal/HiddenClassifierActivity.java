package edu.temple.mar_security.headless_tf_mal;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.temple.mar_security.res_lib.HeadlessVideoActivity;
import edu.temple.mar_security.res_lib_tf.Classifier;
import edu.temple.mar_security.res_lib_tf.Recognition;

public class HiddenClassifierActivity extends HeadlessVideoActivity {

    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------
    //
    //        EDIT THESE PROPERTIES !!
    //
    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------


    // TODO - update the log tag to something appropriate to what you're testing
    private static final String LOG_TAG = "Headless_TF_Malicious";

    // TODO - update the video name to whatever you're using
    private static final String VIDEO_NAME = "long_video.mp4";



    // TODO - update to reflect the name and extension of the TF model
    //  file (stored in project "assets" folder)
    private static final String MODEL_FILENAME = "flower_model.tflite";

    // TODO - update to reflect the name and extension of the TF labels
    //  file (stored in project "assets" folder)
    private static final String LABEL_FILENAME = "flower_labels.txt";



    // TODO - update to reflect the name and extension of the ** HIDDEN / MALICIOUS **
    //  TF model file (stored in project "assets" folder)
    private static final String HIDDEN_MODEL_FILENAME = "hidden_model.tflite";

    // TODO - update to reflect the name and extension of the ** HIDDEN / MALICIOUS **
    //  TF labels file (stored in project "assets" folder)
    private static final String HIDDEN_LABEL_FILENAME = "hidden_labels.txt";


    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------





    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------
    //
    //        LEAVE THE REST OF THE CLASS ALONE
    //
    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------


    private static final Classifier.Device DEFAULT_DEVICE = Classifier.Device.CPU;
    private static final int DEFAULT_NUM_THREADS = 1;

    private int sensorOrientation;

    private long lastProcessingTimeMs;
    private long sysStartTime;

    private Classifier classifier;
    private Classifier hiddenClassifier;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // MUST POPULATE THESE FIELDS ** BEFORE ** CALLING SUPER ON-CREATE
        setContentView(R.layout.activity_main);
        simpleVideoView = findViewById(R.id.simpleVideoView);

        super.onCreate(savedInstanceState);
        Log.d(getLogTag(), "onCreate");

        sensorOrientation = (90 - getScreenOrientation());
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

        if (hiddenClassifier != null) {
            hiddenClassifier.close();
            hiddenClassifier = null;
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
                    DEFAULT_DEVICE, DEFAULT_NUM_THREADS);
        } catch (IOException ex) {
            Log.e(getLogTag(), "Something went wrong while trying to "
                    + "instantiate the classifier!", ex);
            classifier = null;
        }

        try {
            hiddenClassifier = Classifier.create(this, HIDDEN_MODEL_FILENAME,
                    HIDDEN_LABEL_FILENAME, DEFAULT_DEVICE, DEFAULT_NUM_THREADS);
        } catch (IOException ex) {
            Log.e(getLogTag(), "Something went wrong while trying to "
                    + "instantiate the hidden classifier!", ex);
            hiddenClassifier = null;
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
                            final List<Recognition> results =
                                    classifier.recognizeImage(formattedBmp, sensorOrientation);
                            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                            Log.v(getLogTag(), "Found " + results.size()
                                    + " results in " + lastProcessingTimeMs + "ms");
                            for (Recognition result : results) {
                                Log.v(getLogTag(), "\t\t Title: " + result.getTitle()
                                        + " \t\t Confidence: " + result.getConfidence());
                            }
                        }

                        if (hiddenClassifier != null) {
                            final List<Recognition> results =
                                    hiddenClassifier.recognizeImage(formattedBmp, sensorOrientation);
                            Log.e(getLogTag(), "HIDDEN:  Found " + results.size() + " results");
                            for (Recognition result : results) {
                                Log.e(getLogTag(), "HIDDEN:  \t\t Title: " + result.getTitle()
                                        + " \t\t Confidence: " + result.getConfidence());
                            }
                        }

                        processNextFrame();
                    });
        }
    }


}