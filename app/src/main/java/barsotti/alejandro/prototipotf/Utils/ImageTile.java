package barsotti.alejandro.prototipotf.Utils;

import android.graphics.Rect;
import android.graphics.RectF;

public class ImageTile {
    public final RectF mTileRect;

//    public final int mHorizontalPos;
//
//    public final int mVerticalPos;

//    public ImageTile(RectF tileRect, int horizontalPos, int verticalPos) {
    public ImageTile(RectF tileRect) {
        mTileRect = new RectF();
        mTileRect.set(tileRect);
//        mHorizontalPos = horizontalPos;
//        mVerticalPos = verticalPos;
    }

    public String getKey() {
        return mTileRect.left + "@" + mTileRect.top;
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ImageTile && ((ImageTile) obj).getKey().equals(this.getKey());
    }
}