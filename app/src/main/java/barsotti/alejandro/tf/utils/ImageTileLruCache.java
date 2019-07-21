package barsotti.alejandro.tf.utils;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class ImageTileLruCache extends LruCache<String, Bitmap> {
    public ImageTileLruCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getAllocationByteCount();
    }
}
