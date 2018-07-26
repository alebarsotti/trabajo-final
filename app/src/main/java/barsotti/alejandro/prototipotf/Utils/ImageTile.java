package barsotti.alejandro.prototipotf.Utils;

import android.graphics.Rect;
import android.graphics.RectF;

public class ImageTile {
    public final RectF mTileRect;

    public final int mHorizontalPos;

    public final int mVerticalPos;

    public ImageTile(RectF tileRect, int horizontalPos, int verticalPos) {
        mTileRect = new RectF();
        mTileRect.set(tileRect);
        mHorizontalPos = horizontalPos;
        mVerticalPos = verticalPos;
    }

    private String getKey() {
        return mHorizontalPos + "@" + mVerticalPos;
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        return false;
//    }
}