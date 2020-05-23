package edu.temple.mar_security.headless_fb_mal;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;

import edu.temple.mar_security.res_lib.HeadlessVideoActivity;
import edu.temple.mar_security.res_lib_fb.BarcodeProcessor;
import edu.temple.mar_security.res_lib_fb.TextProcessor;
import edu.temple.mar_security.res_lib_fb.interfaces.ProcessorListener;

public class HiddenClassifierActivity extends HeadlessVideoActivity implements ProcessorListener {

    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------
    //
    //        EDIT THESE PROPERTIES !!
    //
    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------


    // TODO - update the log tag to something appropriate to what you're testing
    private static final String LOG_TAG = "Headless_FB_Mal";

    // TODO - update the video name to whatever you're using
    private static final String VIDEO_NAME = "long_video.mp4";

    // TODO - update the type of primary processor you want to use
    private BarcodeProcessor processor;

    // TODO - update the type of hidden processor you want to use
    private TextProcessor hiddenProcessor;

    @Override
    protected void startCollecting() {
        // ------------------------------------------------------------------------------
        // TODO - update processor instantiation
        // ------------------------------------------------------------------------------
        processor = new BarcodeProcessor(this);
        hiddenProcessor = new TextProcessor(this);
        // ------------------------------------------------------------------------------



        // ------------------------------------------------------------------------------
        // LEAVE THE REST OF THE METHOD ALONE !!
        // ------------------------------------------------------------------------------
        sysStartTime = System.currentTimeMillis();
        processNextFrame();
    }


    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------





    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------
    //
    //        LEAVE THE REST OF THE CLASS ALONE
    //
    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------



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

        if (hiddenProcessor != null) {
            hiddenProcessor.stop();
            hiddenProcessor = null;
        }
    }

    @Override
    public String getLogTag() {
        return LOG_TAG;
    }

    @Override
    public String getVideoPath() {
        File moviesDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        File video = new File(moviesDir, VIDEO_NAME);
        return video.getAbsolutePath();
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
            hiddenProcessor.process(convertedBmp);
        }
    }


}