package edu.temple.mar_security.ml_kit;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.ArrayList;
import java.util.List;

public class FaceAnalyzer implements ImageAnalysis.Analyzer {

    public interface FaceAnalysisListener {
        void facesFound(Image image, int rotation, List<Rect> boundingBoxes);
    }

    private FaceAnalysisListener listener;
    private FaceDetectorOptions options;

    public FaceAnalyzer(FaceAnalysisListener listener, boolean accuracyOverSpeed) {
        this.listener = listener;
        if (accuracyOverSpeed) {
            // High-accuracy landmark detection and face classification
            options = new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                    .build();
        } else {
            // Real-time contour detection
            options = new FaceDetectorOptions.Builder()
                    .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                    .build();
        }
    }

    @Override
    @SuppressLint("UnsafeExperimentalUsageError")
    public void analyze(@NonNull ImageProxy imageProxy) {
        final Image mediaImage = imageProxy.getImage();
        final int rotation = imageProxy.getImageInfo().getRotationDegrees();
        if (mediaImage != null) {
            final InputImage image = InputImage.fromMediaImage(mediaImage, rotation);
            FaceDetection.getClient(options).process(image)
                    .addOnSuccessListener(faces -> {
                        try {
                            List<Rect> boundingBoxes = new ArrayList<>();
                            for (Face face : faces) {
                                // TODO - place something on the person's head
                                boundingBoxes.add(face.getBoundingBox());
                            }
                            if (boundingBoxes.size() > 0) {
                                listener.facesFound(mediaImage, rotation, boundingBoxes);
                            }
                        } catch (Exception ex) {
                            Log.e(MainActivity.TAG, "Something went wrong while trying to "
                                    + "process the bounding boxes for detected faces!", ex);
                        }
                    })
                    .addOnFailureListener(ex -> {
                        Log.e(MainActivity.TAG, "Something went wrong while attempting to "
                                + "detect faces in the latest frame!", ex);
                    })
                    .addOnCompleteListener(task -> {
                        imageProxy.close();
                    });
        }
    }

}