package edu.temple.mar_security.mlkit_orth.hidden;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.util.ArrayList;
import java.util.List;

import edu.temple.mar_security.mlkit_orth.MainActivity;
import edu.temple.mar_security.res_lib.BaseActivity;
import edu.temple.mar_security.res_lib.face_detection.FaceAnalyzer;
import edu.temple.mar_security.res_lib.utils.Constants;
import edu.temple.mar_security.res_lib.utils.FileUtil;

public class TextProcessor implements FaceAnalyzer.FaceAnalysisListener {

    private static final String TAG = MainActivity.TAG; // ImageProcessor.class.getSimpleName();

    private Activity mActivity;

    public TextProcessor(Activity parent) {
        this.mActivity = parent;
    }

    @Override
    public void facesFound(Bitmap bitmap, List<Rect> boundingBoxes) {
        if (bitmap != null) {
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            TextRecognizer recognizer = TextRecognition.getClient();
            recognizer.process(image)
                    .addOnSuccessListener(text -> {
                        if (mActivity instanceof BaseActivity) {
                            if (!text.getText().isEmpty()) {
                                String line = text.getText().replace("\n", " ");
                                Log.i(Constants.LOG_TAG, "Found text: " + line);
                                ((BaseActivity) mActivity).logMLEvent(line);
                            }
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