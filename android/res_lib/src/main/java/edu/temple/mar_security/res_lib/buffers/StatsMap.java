package edu.temple.mar_security.res_lib.buffers;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.temple.mar_security.res_lib.utils.Constants;
import edu.temple.mar_security.res_lib.utils.FileUtil;

public class StatsMap {

    private Map<String,String> singleStatsMap = new HashMap<>();
    private Map<String,String[]> multiStatsMap = new HashMap<>();
    private Map<String,List<String[]>> listMultiStatsMap = new HashMap<>();

    public enum Type { Single, Multi, ListMulti }
    private Type mapType;
    private File outputFile;

    public StatsMap(Type mapType, Context context, String outputFileName, String header) {
        this.mapType = mapType;
        this.outputFile = new File(context.getExternalFilesDir(null),
                FileUtil.getTimestampForFile(outputFileName));

        List<String> content = new ArrayList<>();
        content.add(header);
        FileUtil.writeToFile(outputFile, content);
    }

    public void clear() {
        singleStatsMap.clear();
        multiStatsMap.clear();
        listMultiStatsMap.clear();
    }

    public void insert(String value) {
        singleStatsMap.put(FileUtil.getTimestampForFile(), value);
    }

    public void insert(String[] value) {
        multiStatsMap.put(FileUtil.getTimestampForFile(), value);
    }

    public void insert(List<String[]> value) {
        listMultiStatsMap.put(FileUtil.getTimestampForFile(), value);
    }

    public int size() {
        switch (mapType) {
            case Single:
                return singleStatsMap.size();
            case Multi:
                return multiStatsMap.size();
            case ListMulti:
                return listMultiStatsMap.size();
            default:
                return 0;
        }
    }

    public void printToFile() {
        // Log.i(Constants.LOG_TAG, "Writing new stats map to file: " + outputFileName);
        List<String> output = new ArrayList<>();

        switch (mapType) {
            case Single:
                SortedSet<String> keys = new TreeSet<>(singleStatsMap.keySet());
                for (String key : keys) {
                    String value = singleStatsMap.get(key);
                    String line = (key + "," + value);
                    output.add(line);
                }
                break;
            case Multi:
                SortedSet<String> multiKeys = new TreeSet<>(multiStatsMap.keySet());
                for (String key : multiKeys) {
                    String[] value = multiStatsMap.get(key);
                    String line = key;
                    for (String valuePart : value) line += ("," + valuePart);
                    output.add(line);
                }
                break;
            case ListMulti:
                SortedSet<String> listMultiKeys = new TreeSet<>(listMultiStatsMap.keySet());
                for (String key : listMultiKeys) {
                    List<String[]> value = listMultiStatsMap.get(key);
                    String line = key;
                    for (String[] valuePart : value)
                        for (String valuePartPart : valuePart)
                            line += ("," + valuePartPart);
                    output.add(line);
                }
                break;
        }

        if (output.size() > 0) {
            FileUtil.appendToFile(outputFile, output);
            clear();
        } else {
            // Log.e(Constants.LOG_TAG, "No output to write to output file: "
            //         + outputFile.getAbsolutePath());
        }
    }

}