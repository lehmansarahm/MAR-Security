package edu.temple.edge_playground.res_lib.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.temple.edge_playground.res_lib.utils.Constants;

import static edu.temple.edge_playground.res_lib.utils.Constants.LOG_TAG;

public class StorageUtil {

    public static String getTimestamp() {
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat df = new SimpleDateFormat(Constants.TIMESTAMP_STAT_RECORD);
        return df.format(currentTime);
    }

    public static String getTimestamp(String label, String ext) {
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat df = new SimpleDateFormat(Constants.TIMESTAMP_FILENAME);
        if (label != null && !label.isEmpty())
            return (df.format(currentTime) + "_" + label + "." + ext);
        else return df.format(currentTime);
    }

    public static void writeContentToFile(Context context, String filename, List<String> content) {
        File outputDir = verifyFilesDir(context);
        File outputFile = new File(outputDir, filename);

        try {
            Log.d(LOG_TAG, "Writing data to file: " + outputFile.getPath());
            if (!outputFile.exists()) outputFile.createNewFile();

            FileOutputStream stream = new FileOutputStream(outputFile);
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            for (String line : content) {
                writer.append(line).append("\n");
            }

            writer.close();
            stream.flush();
            stream.close();
        } catch (FileNotFoundException ex) {
            Log.e(LOG_TAG, "Unable to access output file.  Cannot write results "
                    + "to file: " + outputFile.getPath(), ex);
        } catch (IOException ex) {
            Log.e(LOG_TAG, "Unable to write to output file, or close output stream "
                    + "to file: " + outputFile.getPath(), ex);
        }
    }

    public static File initializeFile(Context context, String filename, String headerRow) {
        File outputFile = new File(verifyFilesDir(context), filename);
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

    private static File verifyFilesDir(Context context) {
        File outputDir = new File(context.getExternalFilesDir(""), "");
        if (!outputDir.exists()) {
            Log.e(LOG_TAG, "Output dir does not exist.  Creating output dir: "
                    + outputDir.getAbsolutePath());
            boolean success = outputDir.mkdir();
            if (!success) {
                Log.e(LOG_TAG, "Something went wrong while attempting to create "
                        + "output directory: " + outputDir.getAbsolutePath());
            }
        }
        return outputDir;
    }

}