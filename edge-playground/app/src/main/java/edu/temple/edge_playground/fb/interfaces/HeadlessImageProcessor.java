package edu.temple.edge_playground.fb.interfaces;

import android.graphics.Bitmap;

public interface HeadlessImageProcessor {

    void process(Bitmap bitmap);

    void stop();

}