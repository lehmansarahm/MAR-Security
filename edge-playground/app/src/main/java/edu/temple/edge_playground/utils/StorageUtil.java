package edu.temple.edge_playground.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static edu.temple.edge_playground.utils.Constants.LOG_TAG;

public class StorageUtil {

    public static String getTimestamp() {
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat df = new SimpleDateFormat(Constants.TIMESTAMP_STAT_RECORD);
        return df.format(currentTime);
    }

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