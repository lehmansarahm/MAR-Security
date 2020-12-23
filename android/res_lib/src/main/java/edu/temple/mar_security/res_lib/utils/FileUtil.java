package edu.temple.mar_security.res_lib.utils;

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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static edu.temple.mar_security.res_lib.utils.Constants.LOG_TAG;

public class FileUtil {

    // private static final String FILENAME_FORMAT = "yyyyMMdd_HHmmssSSS";
    // private static final SimpleDateFormat SDF = new SimpleDateFormat(FILENAME_FORMAT);

    public static File getOutputDirectory(Context context) {
        File outputDir = context.getExternalFilesDir("");
        if (!outputDir.exists()) outputDir.mkdir();
        return outputDir;
    }

    public static String getTimestampForEntry() {
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat df = new SimpleDateFormat(Constants.TIMESTAMP_STAT_RECORD);
        return df.format(currentTime);
    }

    public static String getTimestampForFile() {
        return getTimestampForFile(null);
    }

    public static String getTimestampForFile(String label) {
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat df = new SimpleDateFormat(Constants.TIMESTAMP_FILENAME);
        if (label != null && !label.isEmpty()) return (df.format(currentTime) + "_" + label);
        else return df.format(currentTime);
    }


    // ---------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------


    public static File initializeFile(Context context, String filename, String headerRow) {
        File outputDir = new File(context.getExternalFilesDir(""), "");
        if (!outputDir.exists()) {
            Log.e(LOG_TAG, "Output dir does not exist.  Creating output dir: "
                    + outputDir.getAbsolutePath());
            boolean success = outputDir.mkdir();
            if (!success) {
                Log.e(LOG_TAG, "Something went wrong while attempting to create "
                        + "output directory: " + outputDir.getAbsolutePath());
                return null;
            }
        }

        File outputFile = new File(outputDir, filename);
        if (!outputFile.exists()) {
            try {
                boolean success = outputFile.createNewFile();
                if (!success) {
                    Log.e(LOG_TAG, "Something went wrong while attempting to create a "
                            + "new output file: " + outputFile.getAbsolutePath());
                    return null;
                }

                appendToFile(outputFile, new String[] { headerRow });
            } catch (IOException ex) {
                Log.e(LOG_TAG, "Something went wrong while attempting to create a "
                        + "new output file: " + outputFile.getAbsolutePath(), ex);
                return null;
            }
        }

        return outputFile;
    }

    public static void writeToFile(Context context, Bitmap image) {
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat df = new SimpleDateFormat(Constants.TIMESTAMP_FILENAME);
        String filename = (df.format(new Date()) + ".png");
        File file = new File(getOutputDirectory(context), filename);

        try {
            Log.i(Constants.LOG_TAG, "Writing image to file: " + file.getAbsolutePath());
            FileOutputStream fOut = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException ex) {
            Log.e(Constants.LOG_TAG, "Something went wrong while attempting to write image to file: "
                    + file.getAbsolutePath(), ex);
        }
    }

    public static void writeToFile(File outputFile, List<String> content) {
        try {
            Log.d(Constants.LOG_TAG, "Writing resource stats to file: " + outputFile.getPath());
            if (!outputFile.exists()) outputFile.createNewFile();

            FileOutputStream stream = new FileOutputStream(outputFile);
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            for (String line : content) { writer.append(line + "\n"); }

            writer.close();
            stream.flush();
            stream.close();
        } catch(FileNotFoundException ex) {
            Log.e(Constants.LOG_TAG, "Unable to access output file.  Cannot write results to file.", ex);
        } catch (IOException ex) {
            Log.e(Constants.LOG_TAG, "Unable to write to output file, or close output stream.", ex);
        }
    }

    public static void appendToFile(File outputFile, String[] data) {
        try {
            FileWriter csvWriter = new FileWriter(outputFile, true);
            for (String rowData : data) {
                Log.i(LOG_TAG, "Opening file: " + outputFile.getAbsolutePath()
                        + "\n\t Appending: " + rowData);
                csvWriter.append(rowData);
                csvWriter.append("\n");
            }

            csvWriter.flush();
            csvWriter.close();
        } catch (IOException ex) {
            Log.e(LOG_TAG, "Something went wrong while trying to write data to file: "
                    + outputFile.getAbsolutePath(), ex);
        }
    }

}