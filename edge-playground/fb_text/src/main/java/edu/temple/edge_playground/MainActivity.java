package edu.temple.edge_playground;

import android.os.Bundle;
import android.util.Log;

import edu.temple.edge_playground.res_lib.fb.FbBaseActivity;

public class MainActivity extends FbBaseActivity {

    // TODO - indicate the type of processing to do
    private static final boolean USE_LOCAL_PROCESSING = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO - update the video name to whatever you're using
        VIDEO_NAME = "text_wall.mp4";

        // TODO - update the processor instantiation
        processor = new TextProcessor(this, USE_LOCAL_PROCESSING);

        // Leave the rest of the method alone ...

        // MUST POPULATE THESE FIELDS ** BEFORE ** CALLING SUPER ON-CREATE
        setContentView(R.layout.activity_main);
        simpleVideoView = findViewById(R.id.simpleVideoView);

        super.onCreate(savedInstanceState);
        Log.d(getLogTag(), "onCreate");
    }

}