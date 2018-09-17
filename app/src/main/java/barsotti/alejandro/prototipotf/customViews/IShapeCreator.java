package barsotti.alejandro.prototipotf.customViews;

import android.graphics.PointF;

public interface IShapeCreator {
//    void addPoint(PointF point);
    void addOnMatrixViewChangeListener(IOnMatrixViewChangeListener listener);
    void removeOnMatrixViewChangeListener(IOnMatrixViewChangeListener listener);
}
