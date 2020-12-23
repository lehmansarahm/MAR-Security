package edu.temple.mar_security.mlkit_orth;

import androidx.camera.core.CameraSelector;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;

import java.util.List;
import java.util.concurrent.Executors;

import edu.temple.mar_security.mlkit_orth.hidden.ImageProcessor;
import edu.temple.mar_security.res_lib.BaseActivity;
import edu.temple.mar_security.res_lib.face_detection.FaceAnalyzer;

public class MainActivity extends BaseActivity implements FaceAnalyzer.FaceAnalysisListener {

    public static final String TAG = "MLKit_Orth";

    // SET TO 'BACK' CAMERA FOR TESTING
    private static final int LENS_DIRECTION = CameraSelector.LENS_FACING_BACK;

    // only detects one face at a time when prioritizing speed over accuracy
    private static final boolean ACCURACY_OVER_SPEED = true;

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

        if (arePermissionsGranted()) moveForward();
        else getRuntimePermissions();
    }

    @Override
    protected void onDestroy() {
        cameraExecutor.shutdown();
        super.onDestroy();
    }

    @Override
    protected void moveForward() {
        startCamera(this, LENS_DIRECTION, ACCURACY_OVER_SPEED);
    }

    @Override
    public void facesFound(Bitmap bitmap, List<Rect> boundingBoxes) {
        for (Rect boundingBox : boundingBoxes) {
            Log.i(TAG, "Found face at: " + boundingBox.top + ", " + boundingBox.left);
        }

        (new ImageProcessor(this)).facesFound(bitmap, boundingBoxes);
    }
}
