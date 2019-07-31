package barsotti.alejandro.tf.utils;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.collection.LruCache;

public class ImageTileLruCache extends LruCache<String, Bitmap> {
    public ImageTileLruCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(@NonNull String key, @NonNull Bitmap value) {
        return value.getAllocationByteCount();
    }
}
