package edu.temple.mar_security.ml_kit;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.common.MlKitException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.temple.mar_security.ml_kit.face_detection.FaceAnalyzer;
import edu.temple.mar_security.ml_kit.face_detection.FaceProcessor;
import edu.temple.mar_security.ml_kit.overlay.GraphicOverlay;

public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    public static final String TAG = "MAR_Security_MLKit";

    private static final boolean ACCURACY_OVER_SPEED = false;
    private static final int LENS_FACING = CameraSelector.LENS_FACING_FRONT;
    private static final boolean IS_IMAGE_FLIPPED = (LENS_FACING == CameraSelector.LENS_FACING_FRONT);

    private static final int PERMISSION_REQUESTS = 999;
    private static final String[] PERMISSIONS = new String[] {
            Manifest.permission.CAMERA
    };

    private ExecutorService cameraExecutor;
    private FaceProcessor processor;

    private PreviewView previewView;
    private GraphicOverlay graphicOverlay;

    private boolean needUpdateGraphicOverlayImageSourceInfo;

    // ----------------------------------------------------------------------------------
    //      ACTIVITY LIFE CYCLE LISTENERS
    // ----------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraExecutor = Executors.newSingleThreadExecutor();
        processor = new FaceProcessor(this);

        previewView = findViewById(R.id.viewFinder);
        graphicOverlay = findViewById(R.id.graphicOverlay);

        if (allPermissionsGranted()) startCamera();
        else getRuntimePermissions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    // ----------------------------------------------------------------------------------
    //      CAMERA CONTROLS
    // ----------------------------------------------------------------------------------

    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            CameraSelector camera = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build();

            Preview preview = new Preview.Builder().build();
            preview.setSurfaceProvider(previewView.createSurfaceProvider());

            needUpdateGraphicOverlayImageSourceInfo = true;
            ImageAnalysis analysis = new ImageAnalysis.Builder().build();
            analysis.setAnalyzer(
                    // imageProcessor.processImageProxy will use another thread to run the detection underneath,
                    // thus we can just runs the analyzer itself on main thread.
                    ContextCompat.getMainExecutor(this), imageProxy -> {
                        if (needUpdateGraphicOverlayImageSourceInfo) {
                            int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                            if (rotationDegrees == 0 || rotationDegrees == 180) {
                                graphicOverlay.setImageSourceInfo(imageProxy.getWidth(),
                                        imageProxy.getHeight(), IS_IMAGE_FLIPPED);
                            } else {
                                graphicOverlay.setImageSourceInfo(imageProxy.getHeight(),
                                        imageProxy.getWidth(), IS_IMAGE_FLIPPED);
                            }
                            needUpdateGraphicOverlayImageSourceInfo = false;
                        }

                        FaceAnalyzer imageAnalyzer =
                                new FaceAnalyzer(processor, ACCURACY_OVER_SPEED, graphicOverlay);
                        imageAnalyzer.analyze(imageProxy);
                    });



            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, camera, preview, analysis);
            } catch (Exception ex) {
                Log.e(TAG, "Something went wrong while trying to start the camera!", ex);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // ----------------------------------------------------------------------------------
    //      PERMISSIONS
    // ----------------------------------------------------------------------------------

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) startCamera();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean allPermissionsGranted() {
        for (String permission : PERMISSIONS) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : PERMISSIONS) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]),
                    PERMISSION_REQUESTS);
        }
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }

}
