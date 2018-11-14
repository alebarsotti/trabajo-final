package barsotti.alejandro.prototipotf.customViews;

public interface IShapeCreator {
    float getOriginalZoom();
    void addOnMatrixViewChangeListener(IOnMatrixViewChangeListener listener);
    void removeOnMatrixViewChangeListener(IOnMatrixViewChangeListener listener);
}
