package com.iobits.tech.app.ai_identifier.utils;
import android.graphics.Bitmap;

public class BitmapHelper {
    private Bitmap bitmap;
    private static final BitmapHelper instance = new BitmapHelper();

    public BitmapHelper() {
    }

    public static BitmapHelper getInstance() {
        return instance;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
