package com.example.user.qrcodescanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

/**
 * Created by ereminilya on 6/1/17.
 */

public class ImageUtils {

    public static Bitmap getBitmapFromFile(String pathToImage) {
        File imgFile = new File(pathToImage);

        if (imgFile.exists()) {
            return BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        } else {
            return null;
        }
    }

}
