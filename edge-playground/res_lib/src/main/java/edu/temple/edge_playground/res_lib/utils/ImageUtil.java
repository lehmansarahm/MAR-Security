package edu.temple.edge_playground.res_lib.utils;

import android.graphics.Bitmap;

public class ImageUtil {

    public static Bitmap formatBitmap(Bitmap srcBmp, int size) {
        Bitmap croppedBmp;
        if (srcBmp.getWidth() >= srcBmp.getHeight()){
            croppedBmp = Bitmap.createBitmap(
                    srcBmp,
                    ((srcBmp.getWidth()/2) - (srcBmp.getHeight()/2)),
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );
        } else {
            croppedBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    ((srcBmp.getHeight()/2) - (srcBmp.getWidth()/2)),
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        }

        Bitmap scaledBmp = Bitmap.createScaledBitmap(croppedBmp, size, size, true);
        Bitmap formattedBmp = scaledBmp.copy(Bitmap.Config.ARGB_8888, false);
        return formattedBmp;
    }
}
