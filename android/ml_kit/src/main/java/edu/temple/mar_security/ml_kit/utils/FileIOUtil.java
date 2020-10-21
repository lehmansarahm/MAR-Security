package edu.temple.mar_security.ml_kit.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.temple.mar_security.ml_kit.MainActivity;

public class FileIOUtil {

    private static final String TAG = MainActivity.TAG;
    private static final String FILENAME_FORMAT = "yyyyMMdd_HHmmssSSS";
    private static final SimpleDateFormat SDF = new SimpleDateFormat(FILENAME_FORMAT);

    public static Bitmap toBitmap(Image image, int rotation) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];
        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21,
                image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0,
                yuvImage.getWidth(), yuvImage.getHeight()), 100, out);

        byte[] imageBytes = out.toByteArray();
        Bitmap original = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        Matrix rotationMatrix = new Matrix();
        rotationMatrix.postRotate((float) rotation);
        Bitmap rotatedBitmap = Bitmap.createBitmap(original, 0, 0,
                original.getWidth(), original.getHeight(), rotationMatrix, true);

        // return original;
        return rotatedBitmap;
    }

    public static Bitmap crop(Bitmap original, Rect boundingBox) {
        Log.i(TAG, "Attempting to crop bitmap with height: " + original.getHeight()
                + " and width: " + original.getWidth()
                + " \t\t using bounding box with top: " + boundingBox.top
                + ", bottom: " + boundingBox.bottom
                + ", left: " + boundingBox.left
                + ", right: " + boundingBox.right
                + ", height: " + boundingBox.height()
                + ", width: " + boundingBox.width());
        return Bitmap.createBitmap(original, boundingBox.left, boundingBox.top,
                boundingBox.width(), boundingBox.height());
    }

    public static File getOutputDirectory(Context context) {
        File outputDir = context.getExternalFilesDir("");
        if (!outputDir.exists()) outputDir.mkdir();
        return outputDir;
    }

    public static void writeToFile(Context context, Bitmap image) {
        String filename = (SDF.format(new Date()) + ".png");
        File file = new File(getOutputDirectory(context), filename);

        try {
            Log.i(TAG, "Writing image to file: " + file.getAbsolutePath());
            FileOutputStream fOut = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException ex) {
            Log.e(TAG, "Something went wrong while attempting to write image to file: "
                    + file.getAbsolutePath(), ex);
        }
    }

}