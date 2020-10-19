package edu.temple.mar_security.ml_kit;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback,
        FaceAnalyzer.FaceAnalysisListener {

    public static final String TAG = "MAR_Security_MLKit";

    private static final boolean ACCURACY_OVER_SPEED = false;
    private static final String FILENAME_FORMAT = "yyyyMMdd_HHmmssSSS";
    private static final SimpleDateFormat SDF = new SimpleDateFormat(FILENAME_FORMAT);

    private static final int PERMISSION_REQUESTS = 999;
    private static final String[] PERMISSIONS = new String[] {
            Manifest.permission.CAMERA
    };

    private ExecutorService cameraExecutor;
    private PreviewView previewView;

    // ----------------------------------------------------------------------------------
    //      ACTIVITY LIFE CYCLE LISTENERS
    // ----------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraExecutor = Executors.newSingleThreadExecutor();
        previewView = findViewById(R.id.viewFinder);

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

            ImageAnalysis analysis = new ImageAnalysis.Builder().build();
            analysis.setAnalyzer(cameraExecutor,
                    new FaceAnalyzer(this, ACCURACY_OVER_SPEED));

            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, camera, preview, analysis);
            } catch (Exception ex) {
                Log.e(TAG, "Something went wrong while trying to start the camera!", ex);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    public void facesFound(Image image, int rotation, List<Rect> boundingBoxes) {
        Log.i(TAG, "Received image with " + boundingBoxes.size()
                + " bounding boxes and rotation: " + rotation);

        try {
            for (Rect boundingBox : boundingBoxes) {
                // extract the faces according to the bounding box coordinates
                Bitmap bitmap = toBitmap(image, rotation);
                Bitmap croppedBitmap = crop(bitmap, boundingBox);
                writeToFile(croppedBitmap);

                // TODO - scale the face snippets to a useful size
                // TODO - extract RGB matrices
                // TODO - feed inputs to Kunal's CV modules
            }
        } catch (Exception ex) {
            Log.e(TAG, "Something went wrong while attempting to process detected faces!", ex);
        }
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

    // ----------------------------------------------------------------------------------
    //      FILE I/O
    // ----------------------------------------------------------------------------------

    private Bitmap toBitmap(Image image, int rotation) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];
        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21,
                image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0,
                yuvImage.getWidth(), yuvImage.getHeight()), 100, out);

        byte[] imageBytes = out.toByteArray();
        Bitmap original = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        Matrix rotationMatrix = new Matrix();
        rotationMatrix.postRotate((float) rotation);
        Bitmap rotatedBitmap = Bitmap.createBitmap(original, 0, 0,
                original.getWidth(), original.getHeight(), rotationMatrix, true);

        // return original;
        return rotatedBitmap;
    }

    private Bitmap crop(Bitmap original, Rect boundingBox) {
        Log.i(TAG, "Attempting to crop bitmap with height: " + original.getHeight()
                + " and width: " + original.getWidth()
                + " \t\t using bounding box with top: " + boundingBox.top
                + ", bottom: " + boundingBox.bottom
                + ", left: " + boundingBox.left
                + ", right: " + boundingBox.right
                + ", height: " + boundingBox.height()
                + ", width: " + boundingBox.width());
        return Bitmap.createBitmap(original, boundingBox.left, boundingBox.top,
                boundingBox.width(), boundingBox.height());
    }

    private File getOutputDirectory() {
        File outputDir = getExternalFilesDir("");
        if (!outputDir.exists()) outputDir.mkdir();
        return outputDir;
    }

    private void writeToFile(Bitmap image) {
        String filename = (SDF.format(new Date()) + ".png");
        File file = new File(getOutputDirectory(), filename);

        try {
            Log.i(TAG, "Writing image to file: " + file.getAbsolutePath());
            FileOutputStream fOut = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException ex) {
            Log.e(TAG, "Something went wrong while attempting to write image to file: "
                    + file.getAbsolutePath(), ex);
        }
    }
}
