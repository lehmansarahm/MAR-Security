package edu.temple.mar_security.res_lib_fb;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import edu.temple.mar_security.res_lib.BaseActivity;
import edu.temple.mar_security.res_lib.utils.Constants;
import edu.temple.mar_security.res_lib_fb.interfaces.HeadlessImageProcessor;
import edu.temple.mar_security.res_lib_fb.interfaces.ProcessorListener;

public class BarcodeProcessor implements HeadlessImageProcessor {

    private final FirebaseVisionBarcodeDetector detector;
    private ProcessorListener listener;
    private Context context;

    public BarcodeProcessor(Context context, ProcessorListener listener) {
        // Note that if you know which format of barcode your app is dealing with, detection will be
        // faster to specify the supported barcode formats one by one, e.g.
        // new FirebaseVisionBarcodeDetectorOptions.Builder()
        //     .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
        //     .build();

        BaseActivity.logMLEvent("FirebaseVision - getInstance() - getVisionBarcodeDetector()");
        detector = FirebaseVision.getInstance().getVisionBarcodeDetector();
        this.listener = listener;
        this.context = context;
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(Constants.LOG_TAG, "Exception thrown while trying to close Barcode Detector: " + e);
        }
    }

    @Override
    public void process(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        Log.i(Constants.LOG_TAG, "Attempting to process image: " + bitmap);

        BaseActivity.logMLEvent("FirebaseVisionBarcodeDetector - detectInImage()");
        Task<List<FirebaseVisionBarcode>> result =
                detector.detectInImage(image).addOnSuccessListener(barcodes -> {
                    BaseActivity.logMLEvent("Begin successful result processing");
                    writeToFile(bitmap);

                    for (int i = 0; i < barcodes.size(); ++i) {
                        FirebaseVisionBarcode barcode = barcodes.get(i);
                        BaseActivity.logMLEvent("Detected barcode: " + barcode.getRawValue()
                                + ", top: " + barcode.getBoundingBox().top
                                + ", left: " + barcode.getBoundingBox().left
                                + ", bottom: " + barcode.getBoundingBox().bottom
                                + ", right: " + barcode.getBoundingBox().right);
                    }

                    BaseActivity.logMLEvent("End successful result processing");
                    if (listener != null) listener.onResultsAvailable();
                }).addOnFailureListener(e -> {
                    Log.e(Constants.LOG_TAG, "Barcode detection failed", e);
                    if (listener != null) listener.onResultsAvailable();
                });

        Log.i(Constants.LOG_TAG, "Was result available? " + (result != null));
    }

    private void writeToFile(final Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        byte[] bytes = stream.toByteArray();

        File outputFile = new File(context.getExternalFilesDir(""), "output.png");
        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            out.write(bytes);
        } catch (IOException e) {
            Log.e(Constants.LOG_TAG, "Something went wrong while attempting to write "
                    + "positive matching bitmap to file: " + outputFile.getAbsolutePath(), e);
        }
    }
}