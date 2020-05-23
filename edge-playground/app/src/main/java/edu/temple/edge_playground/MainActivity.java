package edu.temple.edge_playground;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;

import edu.temple.edge_playground.ref.HeadlessVideoActivity;
import edu.temple.edge_playground.fb.ImageLabelProcessor;
import edu.temple.edge_playground.fb.interfaces.ProcessorListener;
import edu.temple.edge_playground.utils.Constants;

public class MainActivity extends HeadlessVideoActivity implements ProcessorListener {

    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------
    //
    //        EDIT THESE PROPERTIES !!
    //
    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------

    // TODO - update the video name to whatever you're using
    private static final String VIDEO_NAME = "fetch.mp4";

    // TODO - indicate the type of processing to do
    private static final boolean USE_LOCAL_PROCESSING = false;

    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------





    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------
    //
    //        LEAVE THE REST OF THE CLASS ALONE
    //
    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------



    private ImageLabelProcessor processor;
    private long sysStartTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // MUST POPULATE THESE FIELDS ** BEFORE ** CALLING SUPER ON-CREATE
        setContentView(R.layout.activity_main);
        simpleVideoView = findViewById(R.id.simpleVideoView);

        super.onCreate(savedInstanceState);
        Log.d(getLogTag(), "onCreate");
    }

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
        File moviesDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        File video = new File(moviesDir, VIDEO_NAME);
        return video.getAbsolutePath();
    }

    @Override
    protected void startCollecting() {
        processor = new ImageLabelProcessor(this, USE_LOCAL_PROCESSING);
        sysStartTime = System.currentTimeMillis();
        processNextFrame();
    }

    @Override
    public void onResultsAvailable() {
        Log.i(getLogTag(), "Results received!  Sending next frame");
        processNextFrame();
    }

    // ---------------------------------------------------------------------------

    private void processNextFrame() {
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