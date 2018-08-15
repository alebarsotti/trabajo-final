package barsotti.alejandro.prototipotf.Utils;

import android.graphics.Rect;

public class ImageTile {
    public final Rect Rect;
    public final String Key;

    public ImageTile(Rect tileRect, int sampleLevel, int tileSize) {
        Rect = new Rect();
        Rect.set(tileRect);
        Key = tileRect.left + "@" + tileRect.top + "@" + tileSize + "@" + sampleLevel;
    }
}