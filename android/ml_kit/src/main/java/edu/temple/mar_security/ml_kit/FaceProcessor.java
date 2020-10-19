package edu.temple.mar_security.ml_kit;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.Image;
import android.util.Log;

import java.io.File;
import java.util.List;

public class FaceProcessor implements FaceAnalyzer.FaceAnalysisListener {

    private static final String TAG = MainActivity.TAG;

    private Context context;

    public FaceProcessor(Activity parent) {
        this.context = parent.getApplicationContext();
    }

    @Override
    public void facesFound(Image image, int rotation, List<Rect> boundingBoxes) {
        Log.i(TAG, "Received image with " + boundingBoxes.size()
                + " bounding boxes and rotation: " + rotation);

        try {
            for (Rect boundingBox : boundingBoxes) {
                // extract the faces according to the bounding box coordinates
                cropFace(image, rotation, boundingBox);

                // TODO - scale the face snippets to a useful size
                scaleFace();

                // TODO - extract RGB matrices
                extractFeatureVectors();

                // TODO - feed inputs to Kunal's CV modules
                performAdditionalProcessing();
            }
        } catch (Exception ex) {
            Log.e(TAG, "Something went wrong while attempting to "
                    + "process detected faces!", ex);
        }
    }

    // ----------------------------------------------------------------------------------
    //      PRIVATE METHODS
    // ----------------------------------------------------------------------------------

    private Bitmap cropFace(Image image, int rotation, Rect boundingBox) {
        Bitmap bitmap = FileIOUtil.toBitmap(image, rotation);
        Bitmap croppedBitmap = FileIOUtil.crop(bitmap, boundingBox);
        FileIOUtil.writeToFile(context, croppedBitmap);
        return croppedBitmap;
    }

    private Bitmap scaleFace() {
        return null;
    }

    private byte[] extractFeatureVectors() {
        return null;
    }

    private void performAdditionalProcessing() {
        // empty
    }

}
