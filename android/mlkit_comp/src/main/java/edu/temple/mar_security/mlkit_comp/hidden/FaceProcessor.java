package edu.temple.mar_security.mlkit_comp.hidden;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.temple.mar_security.mlkit_comp.MainActivity;
import edu.temple.mar_security.res_lib.BaseActivity;
import edu.temple.mar_security.res_lib.face_detection.FaceAnalyzer;
import edu.temple.mar_security.res_lib.utils.Constants;
import edu.temple.mar_security.res_lib.utils.FileUtil;
import edu.temple.mar_security.res_lib.utils.ImageUtil;

public class FaceProcessor implements FaceAnalyzer.FaceAnalysisListener {

    private static final String TAG = MainActivity.TAG;

    private Context context;
    private Activity mActivity;

    public FaceProcessor(Activity parent) {
        this.context = parent.getApplicationContext();
        this.mActivity = parent;
    }

    @Override
    public void facesFound(Bitmap bitmap, List<Rect> boundingBoxes) {
        Log.i(TAG, "Received image with " + boundingBoxes.size()
                + " bounding boxes");

        try {
            for (Rect boundingBox : boundingBoxes) {
                Bitmap faceImg = ImageUtil.crop(bitmap, boundingBox); // cropFace(bitmap, boundingBox);
                faceImg = Bitmap.createScaledBitmap(faceImg, 224, 224, false); // scaleFace(faceImg);
                performAdditionalProcessing(faceImg);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Something went wrong while attempting to "
                    + "process detected faces!", ex);
        }
    }

    private void performAdditionalProcessing(Bitmap model_input) {
        Classifier ageClassifier = null;
        Classifier genderClassifier = null;

        try {
            ageClassifier = new AgeClassifier(mActivity);
        } catch (IOException e) {
            Log.e(TAG, "Something went wrong while loading Age classifier");
        }

        final List<Classifier.Recognition> resultsAge =
                ageClassifier.recognizeImage(model_input);
        processResults(resultsAge);

        try {
            genderClassifier = new GenderClassifier(mActivity);
        } catch (IOException e) {
            Log.e(TAG, "Something went wrong while loading Gender classifier");
        }

        final List<Classifier.Recognition> resultsGender =
                genderClassifier.recognizeImage(model_input);
        processResults(resultsGender);
    }

    private void processResults(List<Classifier.Recognition> results) {
        if (mActivity instanceof BaseActivity) {
            String finalResults = "";
            for (Classifier.Recognition result : results) {
                finalResults += (result.getTitle() + "," + result.getConfidence() + "\n");
            }
            ((BaseActivity)mActivity).logMLEvent(finalResults);
        } else {
            Log.e(Constants.LOG_TAG, "Can't log ML event if parent activity "
                    + "doesn't extend BaseActivity class!");
        }
    }

}
