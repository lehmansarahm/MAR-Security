package edu.temple.mar_security.res_lib_fb;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;

import java.io.IOException;
import java.util.List;

import edu.temple.mar_security.res_lib.BaseActivity;
import edu.temple.mar_security.res_lib.utils.Constants;
import edu.temple.mar_security.res_lib_fb.interfaces.HeadlessImageProcessor;
import edu.temple.mar_security.res_lib_fb.interfaces.ProcessorListener;

public class ObjectProcessor implements HeadlessImageProcessor {

    private static final String TAG = Constants.LOG_TAG;

    private final FirebaseVisionObjectDetector detector;
    private ProcessorListener listener;

    public ObjectProcessor(ProcessorListener listener) {
        BaseActivity.logMLEvent("FirebaseVisionObjectDetectorOptions - build()");
        FirebaseVisionObjectDetectorOptions options =
                new FirebaseVisionObjectDetectorOptions.Builder()
                        .setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE)
                        .enableClassification().build();

        BaseActivity.logMLEvent("FirebaseVision - getInstance() - getOnDeviceObjectDetector()");
        detector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options);
        this.listener = listener;
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Text Detector: " + e);
        }
    }

    @Override
    public void process(Bitmap bitmap) {
        Bitmap convertedBitmap = convert(bitmap, Bitmap.Config.ARGB_8888);
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(convertedBitmap);
        Log.i(TAG, "Attempting to process image: " + bitmap);

        BaseActivity.logMLEvent("FirebaseVisionObjectDetector - processImage()");
        Task<List<FirebaseVisionObject>> result =
                detector.processImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionObject>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionObject> results) {
                        BaseActivity.logMLEvent("Begin successful result processing");

                        for (FirebaseVisionObject object : results) {
                            BaseActivity.logMLEvent("Detected object: " + object.toString()
                                    + ", top: " + object.getBoundingBox().top
                                    + ", left: " + object.getBoundingBox().left
                                    + ", bottom: " + object.getBoundingBox().bottom
                                    + ", right: " + object.getBoundingBox().right);
                        }

                        BaseActivity.logMLEvent("End successful result processing");
                        if (listener != null) listener.onResultsAvailable();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Object detection failed", e);
                        if (listener != null) listener.onResultsAvailable();
                    }
                });

        Log.i(TAG, "Was result available? " + (result != null));
    }

    private Bitmap convert(Bitmap bitmap, Bitmap.Config config) {
        Bitmap convertedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), config);
        Canvas canvas = new Canvas(convertedBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return convertedBitmap;
    }

}