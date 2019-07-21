package barsotti.alejandro.tf.utils;

import android.graphics.Rect;

public class ImageTile {
    public final Rect Rect;
    public final String Key;

    public ImageTile(Rect tileRect, int sampleLevel, int tileSize) {
        Rect = new Rect();
        Rect.set(tileRect);
        Key = tileRect.left + "@" + tileRect.top + "@" + tileSize + "@" + sampleLevel;
    }

    @Override
    public String toString() {
        return "ImageTile:" + Key;
    }
}