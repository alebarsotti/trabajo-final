package barsotti.alejandro.prototipotf.interfaces;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;

public interface IZoomableImageView {
    void setRegionBitmap(Bitmap bitmap);

    void setRegionRect(RectF rectF);
}
