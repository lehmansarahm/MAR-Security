package edu.temple.edge_playground;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.temple.edge_playground.res_lib.fb.interfaces.ProcessorListener;
import edu.temple.edge_playground.res_lib.fb.interfaces.HeadlessImageProcessor;
import edu.temple.edge_playground.res_lib.ref.BaseActivity;
import edu.temple.edge_playground.res_lib.utils.Constants;
import edu.temple.edge_playground.res_lib.utils.StorageUtil;

public class TextProcessor implements HeadlessImageProcessor {

    private static final String TAG = Constants.LOG_TAG;

    private final FirebaseVisionTextRecognizer detector;
    private ProcessorListener listener;
    private boolean isRunningLocally;

    public TextProcessor(ProcessorListener listener, boolean runOnDevice) {
        this.listener = listener;
        this.isRunningLocally = runOnDevice;

        if (isRunningLocally) {
            BaseActivity.logMLEvent("FirebaseVision - getInstance() - getOnDeviceTextRecognizer()");
            detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        } else {
            BaseActivity.logMLEvent("FirebaseVision - getInstance() - getCloudTextRecognizer()");
            detector = FirebaseVision.getInstance().getCloudTextRecognizer();
        }
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
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        Log.i(TAG, "Attempting to process image: " + bitmap);

        long startTime = System.currentTimeMillis();
        BaseActivity.logMLEvent("FirebaseVisionTextRecognizer - processImage()");
        Task<FirebaseVisionText> result =
                detector.processImage(image).addOnSuccessListener(results -> {
                    long endTime = System.currentTimeMillis();
                    BaseActivity.logEdgeEvent((endTime - startTime),
                            results.getTextBlocks().size(), isRunningLocally);

                    BaseActivity.logMLEvent("Begin successful result processing");
                    List<FirebaseVisionText.TextBlock> blocks = results.getTextBlocks();
                    for (int i = 0; i < blocks.size(); i++) {
                        List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
                        for (int j = 0; j < lines.size(); j++) {
                            List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                            for (int k = 0; k < elements.size(); k++) {
                                FirebaseVisionText.Element element = elements.get(k);
                                BaseActivity.logMLEvent("Detected text element: " + element.getText());
                                HIDDEN_OPS.add(StorageUtil.getTimestamp() + "," + element.getText());
                            }
                        }
                    }

                    BaseActivity.logMLEvent("End successful result processing");
                    if (listener != null) listener.onResultsAvailable();
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Barcode detection failed", e);
                    if (listener != null) listener.onResultsAvailable();
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