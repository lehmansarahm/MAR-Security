package edu.temple.edge_playground.res_lib.fb.interfaces;

import android.graphics.Bitmap;

public interface HeadlessImageProcessor {

    void process(Bitmap bitmap);

    void stop();

}