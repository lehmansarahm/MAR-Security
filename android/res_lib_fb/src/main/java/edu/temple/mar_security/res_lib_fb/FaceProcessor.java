package edu.temple.mar_security.res_lib_fb;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.temple.mar_security.res_lib.BaseActivity;
import edu.temple.mar_security.res_lib.utils.Constants;
import edu.temple.mar_security.res_lib.utils.StorageUtil;
import edu.temple.mar_security.res_lib_fb.interfaces.HeadlessImageProcessor;
import edu.temple.mar_security.res_lib_fb.interfaces.ProcessorListener;

public class FaceProcessor implements HeadlessImageProcessor {

    private static final String TAG = Constants.LOG_TAG;

    private final FirebaseVisionFaceDetector detector;
    private ProcessorListener listener;

    public FaceProcessor(ProcessorListener listener) {
        // Real-time contour detection of multiple faces
        FirebaseVisionFaceDetectorOptions realTimeOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                        .build();

        BaseActivity.logMLEvent("FirebaseVision - getInstance() - getVisionFaceDetector()");
        detector = FirebaseVision.getInstance().getVisionFaceDetector(realTimeOpts);
        this.listener = listener;
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: " + e);
        }
    }

    @Override
    public void process(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        Log.i(TAG, "Attempting to process image: " + bitmap);

        BaseActivity.logMLEvent("FirebaseVisionFaceDetector - processImage()");
        Task<List<FirebaseVisionFace>> result =
                detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> faces) {
                        BaseActivity.logMLEvent("Begin successful result processing");

                        for (int i = 0; i < faces.size(); ++i) {
                            FirebaseVisionFace face = faces.get(i);
                            BaseActivity.logMLEvent("Detected face: " + face.getTrackingId()
                                    + ", top: " + face.getBoundingBox().top
                                    + ", left: " + face.getBoundingBox().left
                                    + ", bottom: " + face.getBoundingBox().bottom
                                    + ", right: " + face.getBoundingBox().right);

                            HIDDEN_OPS.add(StorageUtil.getTimestamp() + ",Detected face: " + face.getTrackingId()
                                    + ", top: " + face.getBoundingBox().top
                                    + ", left: " + face.getBoundingBox().left
                                    + ", bottom: " + face.getBoundingBox().bottom
                                    + ", right: " + face.getBoundingBox().right);
                        }

                        BaseActivity.logMLEvent("End successful result processing");
                        if (listener != null) listener.onResultsAvailable();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Barcode detection failed", e);
                        if (listener != null) listener.onResultsAvailable();
                    }
                });

        Log.i(TAG, "Was result available? " + (result != null));
    }

    // ------------------------------------------------------------------------------------

    private static List<String> HIDDEN_OPS = new ArrayList<>();

    public static String[] getHiddenOpsArray() {
        return HIDDEN_OPS.toArray(new String[0]);
    }

    public static void clearHiddenOps() {
        HIDDEN_OPS.clear();
    }

}