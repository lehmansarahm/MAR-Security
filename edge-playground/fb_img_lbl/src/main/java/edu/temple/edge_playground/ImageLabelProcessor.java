package edu.temple.edge_playground;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;

import java.io.IOException;
import java.util.List;

import edu.temple.edge_playground.res_lib.fb.interfaces.ProcessorListener;
import edu.temple.edge_playground.res_lib.fb.interfaces.HeadlessImageProcessor;
import edu.temple.edge_playground.res_lib.ref.BaseActivity;
import edu.temple.edge_playground.res_lib.utils.Constants;

public class ImageLabelProcessor implements HeadlessImageProcessor {

    private final FirebaseVisionImageLabeler detector;
    private ProcessorListener listener;
    private boolean isRunningLocally;

    public ImageLabelProcessor(ProcessorListener listener, boolean runOnDevice) {
        this.listener = listener;
        this.isRunningLocally = runOnDevice;

        if (isRunningLocally) {
            BaseActivity.logMLEvent("FirebaseVision - getInstance() - getOnDeviceImageLabeler()");
            detector = FirebaseVision.getInstance().getOnDeviceImageLabeler();
        } else {
            BaseActivity.logMLEvent("FirebaseVision - getInstance() - getCloudImageLabeler()");
            detector = FirebaseVision.getInstance().getCloudImageLabeler();
        }
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(Constants.LOG_TAG, "Exception thrown while trying to "
                    + "close Image Label Detector: " + e);
        }
    }

    @Override
    public void process(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        Log.i(Constants.LOG_TAG, "Attempting to process image: " + bitmap);

        long startTime = System.currentTimeMillis();
        BaseActivity.logMLEvent("FirebaseVisionImageLabeler - processImage()");
        Task<List<FirebaseVisionImageLabel>> result =
                detector.processImage(image).addOnSuccessListener(labels -> {
                    long endTime = System.currentTimeMillis();
                    BaseActivity.logEdgeEvent((endTime - startTime), labels.size(), isRunningLocally);

                    BaseActivity.logMLEvent("Begin successful result processing");
                    for (int i = 0; i < labels.size(); ++i) {
                        BaseActivity.logMLEvent("Detected image label: " + labels.get(i).getText()
                                + " \t with confidence: " + labels.get(i).getConfidence());
                    }

                    BaseActivity.logMLEvent("End successful result processing");
                    if (listener != null) listener.onResultsAvailable();
                }).addOnFailureListener(e -> {
                    Log.e(Constants.LOG_TAG, "Image label generation failed: "
                            + e.getLocalizedMessage(), e);
                    if (listener != null) listener.onResultsAvailable();
                });

        Log.i(Constants.LOG_TAG, "Was result available? " + (result != null));
    }
}