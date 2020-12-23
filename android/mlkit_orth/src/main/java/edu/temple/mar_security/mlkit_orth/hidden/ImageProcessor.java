package edu.temple.mar_security.mlkit_orth.hidden;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.ArrayList;
import java.util.List;

import edu.temple.mar_security.mlkit_orth.MainActivity;
import edu.temple.mar_security.res_lib.BaseActivity;
import edu.temple.mar_security.res_lib.face_detection.FaceAnalyzer;
import edu.temple.mar_security.res_lib.utils.Constants;

public class ImageProcessor implements FaceAnalyzer.FaceAnalysisListener {

    private static final String TAG = MainActivity.TAG; // ImageProcessor.class.getSimpleName();

    private Activity mActivity;

    public ImageProcessor(Activity parent) {
        this.mActivity = parent;
    }

    @Override
    public void facesFound(Bitmap bitmap, List<Rect> boundingBoxes) {
        if (bitmap != null) {
            InputImage image = InputImage.fromBitmap(bitmap, 0); // InputImage.fromMediaImage(rawImage, rotation);
            ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
            labeler.process(image)
                    .addOnSuccessListener(labels -> {
                        if (mActivity instanceof BaseActivity) {
                            List<String[]> results = new ArrayList<>();
                            for (ImageLabel label : labels) {
                                results.add(new String[] {
                                        label.getText(),
                                        String.valueOf(label.getConfidence())
                                });
                            }
                            ((BaseActivity)mActivity).logMLEvent(results);
                        } else {
                            Log.e(Constants.LOG_TAG, "Can't log ML event if parent activity "
                                    + "doesn't extend BaseActivity class!");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Something went wrong while attempting to label "
                                + "the provided image!", e);
                    });
        }
    }

}