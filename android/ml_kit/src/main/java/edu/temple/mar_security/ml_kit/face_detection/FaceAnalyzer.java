package edu.temple.mar_security.ml_kit.face_detection;

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

import edu.temple.mar_security.ml_kit.MainActivity;
import edu.temple.mar_security.ml_kit.overlay.GraphicOverlay;

public class FaceAnalyzer implements ImageAnalysis.Analyzer {

    public interface FaceAnalysisListener {
        void facesFound(Image image, int rotation, List<Rect> boundingBoxes);
    }

    private FaceAnalysisListener listener;
    private FaceDetectorOptions options;
    private GraphicOverlay overlay;

    public FaceAnalyzer(FaceAnalysisListener listener, boolean accuracyOverSpeed, GraphicOverlay overlay) {
        this.listener = listener;
        this.overlay = overlay;

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
                            overlay.clear();
                            List<Rect> boundingBoxes = new ArrayList<>();

                            for (Face face : faces) {
                                overlay.add(new FaceGraphic(overlay, face));
                                boundingBoxes.add(face.getBoundingBox());
                            }

                            if (boundingBoxes.size() > 0) {
                                listener.facesFound(mediaImage, rotation, boundingBoxes);
                            }

                            overlay.postInvalidate();
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