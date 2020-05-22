package edu.temple.mar_security.res_lib_fb.interfaces;

import android.graphics.Bitmap;

public interface HeadlessImageProcessor {

    void process(Bitmap bitmap);

    void stop();

}