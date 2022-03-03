package edu.temple.mar_security.tensorflow;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageProxy;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

import edu.temple.mar_security.res_lib.BaseActivity;
import edu.temple.mar_security.res_lib.utils.ImageUtil;

import edu.temple.mar_security.res_lib_tf.Classifier;
import edu.temple.mar_security.res_lib_tf.RecognitionResult;

public class MainActivity extends BaseActivity {

    public static final String TAG = "Tensorflow";

    private static final int LENS_DIRECTION = CameraSelector.LENS_FACING_BACK;
    private static final String MODEL_FILENAME = "model.tflite";
    private static final String LABEL_FILENAME = "labels.txt";

    private static final Classifier.Device DEFAULT_DEVICE = Classifier.Device.CPU;
    private static final int DEFAULT_NUM_THREADS = 1;

    private int sensorOrientation;
    private Classifier classifier;

    // ----------------------------------------------------------------------------------
    //      ACTIVITY LIFE CYCLE LISTENERS
    // ----------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.mAppName = TAG;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraExecutor = Executors.newSingleThreadExecutor();
        previewView = findViewById(R.id.viewFinder);
        graphicOverlay = findViewById(R.id.graphicOverlay);

        sensorOrientation = (90 - getScreenOrientation());

        if (arePermissionsGranted()) moveForward();
        else getRuntimePermissions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();

        if (classifier != null) {
            classifier.close();
            classifier = null;
        }
    }

    // ----------------------------------------------------------------------------------
    //      BASE CLASS - ABSTRACT METHODS
    // ----------------------------------------------------------------------------------

    @Override
    protected void moveForward() {
        try {
            classifier = Classifier.create(this, MODEL_FILENAME, LABEL_FILENAME,
                    DEFAULT_DEVICE, DEFAULT_NUM_THREADS);
        } catch (IOException ex) {
            Log.e(TAG, "Something went wrong while trying to instantiate the classifier!", ex);
            classifier = null;
        }

        startCamera(LENS_DIRECTION);
    }

    @Override
    @SuppressLint("UnsafeExperimentalUsageError")
    protected void analyze(ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        int rotation = imageProxy.getImageInfo().getRotationDegrees();
        Bitmap bitmap = ImageUtil.toBitmap(mediaImage, rotation);

        final Bitmap formattedBmp = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        runInBackground(() -> {
            if (classifier != null) {
                final List<RecognitionResult> results =
                        classifier.recognizeImage(formattedBmp, sensorOrientation);
                for (RecognitionResult result : results) {
                    Log.v(TAG, "\t\t Title: " + result.getTitle()
                            + " \t\t Confidence: " + result.getConfidence());
                }
            }
        });
    }

}