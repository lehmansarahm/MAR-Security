package edu.temple.mar_security.res_lib;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.view.Choreographer;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import edu.temple.mar_security.res_lib.buffers.StatsMap;
import edu.temple.mar_security.res_lib.overlay.GraphicOverlay;
import edu.temple.mar_security.res_lib.utils.Constants;

import static edu.temple.mar_security.res_lib.utils.Constants.LOG_TAG;
import static edu.temple.mar_security.res_lib.utils.Constants.PERMISSION_REQUESTS;

public abstract class BaseActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String FRAME_HEADER = "Timestamp,Frame Count,Total Frame Time (millis),Avg Frame Time (millis),Avg FPS";
    private static final String FRAME_FILENAME = "FrameStats.csv";
    private static StatsMap mFrameStats;

    private static final String ML_EVENT_HEADER = "Timestamp,Result Title,Result Confidence";
    private static final String ML_EVENT_FILENAME = "MLEvents.csv";
    private static StatsMap mlEventStats;

    private static final long BUFFER_TIME_LIMIT = TimeUnit.SECONDS.toMillis(1);
    private static List<long[]> mFrameBuffer = new ArrayList<>();

    private static long lastSystemTime = 0, currentBufferTime = 0;
    private static int frameCounter = 0;

    protected String mAppName;
    protected int mAppPID;

    private Handler handler;
    private HandlerThread handlerThread;

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppPID = android.os.Process.myPid();
        mFrameStats = new StatsMap(StatsMap.Type.Multi, this, FRAME_FILENAME, FRAME_HEADER);
        mlEventStats = new StatsMap(StatsMap.Type.Single, this, ML_EVENT_FILENAME, ML_EVENT_HEADER);
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        choreographer.postFrameCallback(frameCallback);

        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public synchronized void onPause() {
        handlerThread.quitSafely();

        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
            Log.e(LOG_TAG, "Exception!", e);
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        stopCollection();
        super.onDestroy();
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    protected abstract void moveForward();

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(LOG_TAG, "Permission granted!");
        if (arePermissionsGranted()) moveForward();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected boolean arePermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    protected void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    private Choreographer choreographer = Choreographer.getInstance();
    private Choreographer.FrameCallback frameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            // get the time taken from last frame to this one
            long currentSystemTime = SystemClock.elapsedRealtime();
            long currentFrameTime = (lastSystemTime == 0) ? 0 : (currentSystemTime - lastSystemTime);

            // put the current frame number / time in the frame buffer
            mFrameBuffer.add(new long[] { frameCounter, currentFrameTime });
            currentBufferTime += currentFrameTime;

            if (currentBufferTime >= BUFFER_TIME_LIMIT) {
                // Log.i(Constants.LOG_TAG, "Reached buffer time limit with current buffer time: "
                //         + currentBufferTime);

                // we've filled up our buffer ... retrieve the beginning and ending entries
                long[] firstEntry = mFrameBuffer.get(0);
                long[] lastEntry = mFrameBuffer.get(mFrameBuffer.size() - 1);
                // Log.i(Constants.LOG_TAG, "Consolidating buffer with first entry: { "
                //         + firstEntry[0] + ", " + firstEntry[1] + " } \t\t ... and last entry: { "
                //         + lastEntry[0] + ", " + lastEntry[1] + " }");

                // find the total and average frame times represented by this buffer
                double totalFrameTime = 0.0;
                for (long[] entry : mFrameBuffer) totalFrameTime += entry[1];
                double averageFrameTime = (totalFrameTime / mFrameBuffer.size());

                // dump the details to the stats map
                String[] bufferStats = new String[] {
                        String.valueOf(mFrameBuffer.size()),    // number of frames in buffer
                        String.valueOf(totalFrameTime),
                        String.valueOf(averageFrameTime),
                        String.valueOf(mFrameBuffer.size()/totalFrameTime/1000)
                };
                mFrameStats.insert(bufferStats);

                // reset buffer, associated properties
                mFrameBuffer.clear();
                currentBufferTime = 0;
            }

            // update our variables for the next frame
            lastSystemTime = currentSystemTime;
            frameCounter++;

            // write frame logging / ML event results to file
            mFrameStats.printToFile();
            mlEventStats.printToFile();

            // callback automatically removed ... have to re-associate...
            choreographer.postFrameCallback(this);
        }
    };

    public void logMLEvent(String event) {
        Log.i(LOG_TAG, "Identified ML event: " + event);
        mlEventStats.insert(event);
    }

    private void stopCollection() {
        // release all local variables
        choreographer.removeFrameCallback(frameCallback);
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info = this.getPackageManager().getPackageInfo(this.getPackageName(),
                    PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) return ps;
            else return new String[0];
        } catch (Exception e) {
            return new String[0];
        }
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(LOG_TAG, "Permission granted: " + permission);
            return true;
        }

        Log.i(LOG_TAG, "Permission NOT granted: " + permission);
        return false;
    }

    // ----------------------------------------------------------------------------------
    //      CAMERA CONTROLS
    // ----------------------------------------------------------------------------------

    protected ExecutorService cameraExecutor;
    protected PreviewView previewView;
    protected GraphicOverlay graphicOverlay;

    protected boolean needUpdateGraphicOverlayImageSourceInfo;

    protected abstract void analyze(ImageProxy imageProxy);

    protected void startCamera(int lensDirection) {
                                // (FaceAnalyzer.FaceAnalysisListener listener,
                                // int lensDirection, boolean accuracyOverSpeed) {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);
        final boolean isImageFlipped = (lensDirection == CameraSelector.LENS_FACING_FRONT);

        cameraProviderFuture.addListener(() -> {
            CameraSelector camera = new CameraSelector.Builder()
                    .requireLensFacing(lensDirection)
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
                                        imageProxy.getHeight(), isImageFlipped);
                            } else {
                                graphicOverlay.setImageSourceInfo(imageProxy.getHeight(),
                                        imageProxy.getWidth(), isImageFlipped);
                            }
                            needUpdateGraphicOverlayImageSourceInfo = false;
                        }

                        analyze(imageProxy);
                        /* FaceAnalyzer imageAnalyzer =
                                new FaceAnalyzer(listener, accuracyOverSpeed, graphicOverlay);
                        imageAnalyzer.analyze(imageProxy); */
                    });

            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, camera, preview, analysis);
            } catch (Exception ex) {
                Log.e(LOG_TAG, "Something went wrong while trying to start the camera!", ex);
            }
        }, ContextCompat.getMainExecutor(this));
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