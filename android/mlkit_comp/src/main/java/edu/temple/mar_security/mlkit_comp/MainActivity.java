package edu.temple.mar_security.mlkit_comp;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageProxy;

import java.util.List;
import java.util.concurrent.Executors;

import edu.temple.mar_security.mlkit_comp.hidden.FaceProcessor;
import edu.temple.mar_security.res_lib.BaseActivity;
import edu.temple.mar_security.res_lib.face_detection.FaceAnalyzer;

public class MainActivity extends BaseActivity implements FaceAnalyzer.FaceAnalysisListener {

    public static final String TAG = "MLKit_Comp";

    // SET TO 'BACK' CAMERA FOR TESTING
    private static final int LENS_DIRECTION = CameraSelector.LENS_FACING_BACK;

    // only detects one face at a time when prioritizing speed over accuracy
    private static final boolean ACCURACY_OVER_SPEED = true;

    private FaceProcessor processor;

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

        processor = new FaceProcessor(MainActivity.this);

        if (arePermissionsGranted()) moveForward();
        else getRuntimePermissions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    @Override
    protected void moveForward() {
        startCamera(LENS_DIRECTION);
    }

    @Override
    protected void analyze(ImageProxy imageProxy) {
        FaceAnalyzer imageAnalyzer =
                new FaceAnalyzer(this, ACCURACY_OVER_SPEED, graphicOverlay);
        imageAnalyzer.analyze(imageProxy);
    }

    @Override
    public void facesFound(Bitmap bitmap, List<Rect> boundingBoxes) {
        for (Rect boundingBox : boundingBoxes) {
            Log.i(TAG, "Found face at: " + boundingBox.top + ", " + boundingBox.left);
        }
        processor.facesFound(bitmap, boundingBoxes);
    }
}
