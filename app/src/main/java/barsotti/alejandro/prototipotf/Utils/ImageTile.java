package barsotti.alejandro.prototipotf.Utils;

import android.graphics.RectF;

public class ImageTile {
    public final RectF Rect;
    public final String Key;
    public final int SampleLevel;

//    public final int mHorizontalPos;
//
//    public final int mVerticalPos;

//    public ImageTile(RectF tileRect, int horizontalPos, int verticalPos) {
    public ImageTile(RectF tileRect, int sampleLevel) {
        Rect = new RectF();
        Rect.set(tileRect);
        SampleLevel = sampleLevel;
        Key = tileRect.left + "@" + tileRect.top + "@" + sampleLevel;
//        mHorizontalPos = horizontalPos;
//        mVerticalPos = verticalPos;
    }

//    public String getKey() {
//        return Rect.left + "@" + Rect.top;
//    }

//    @Override
//    public int hashCode() {
//        return getKey().hashCode();
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        return obj instanceof ImageTile && ((ImageTile) obj).getKey().equals(this.getKey());
//    }
}