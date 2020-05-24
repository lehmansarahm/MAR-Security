package edu.temple.edge_playground.res_lib.fb;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;

import edu.temple.edge_playground.res_lib.fb.interfaces.HeadlessImageProcessor;
import edu.temple.edge_playground.res_lib.fb.interfaces.ProcessorListener;
import edu.temple.edge_playground.res_lib.ref.HeadlessVideoActivity;
import edu.temple.edge_playground.res_lib.utils.Constants;

public abstract class FbBaseActivity extends HeadlessVideoActivity implements ProcessorListener {

    protected String VIDEO_NAME;

    protected HeadlessImageProcessor processor;
    protected long sysStartTime;

    @Override
    public void onResume() {
        super.onResume();
        Log.d(getLogTag(), "onResume");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (processor != null) {
            processor.stop();
            processor = null;
        }
    }

    @Override
    public String getLogTag() {
        return Constants.LOG_TAG;
    }

    @Override
    public String getVideoPath() {
        try {
            File moviesDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
            Log.i(Constants.LOG_TAG, "Attempting to retrieve video from directory: "
                    + moviesDir.getAbsolutePath());

            for (File childFile : moviesDir.listFiles()) {
                Log.i(Constants.LOG_TAG, "Movies dir child: " + childFile.getAbsolutePath());
            }

            Log.i(Constants.LOG_TAG, "Attempting to retrieve video: " + VIDEO_NAME
                    + " \t from directory: " + moviesDir.getAbsolutePath());

            File video = new File(moviesDir, VIDEO_NAME);
            Log.i(Constants.LOG_TAG, "Returning video path: " + video.getAbsolutePath());

            return video.getAbsolutePath();
        } catch (Exception ex) {
            Log.e(Constants.LOG_TAG, "Something went wrong while trying to "
                    + "retrieve the video path!", ex);
            return "";
        }
    }

    @Override
    protected void startCollecting() {
        sysStartTime = System.currentTimeMillis();
        processNextFrame();
    }

    @Override
    public void onResultsAvailable() {
        Log.i(getLogTag(), "Results received!  Sending next frame");
        processNextFrame();
    }

    // ---------------------------------------------------------------------------

    protected void processNextFrame() {
        long currentTime = System.currentTimeMillis();
        long frameTimeMicros = ((currentTime - sysStartTime) * 1000);
        Log.i(getLogTag(), "Sending frame to video source with timestamp: " + frameTimeMicros);

        Bitmap frameBmp = retriever.getFrameAtTime(frameTimeMicros);
        if (frameBmp != null) {
            Bitmap convertedBmp = frameBmp.copy(Bitmap.Config.ARGB_8888, true);
            processor.process(convertedBmp);
        }
    }
}
