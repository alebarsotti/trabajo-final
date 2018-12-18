package barsotti.alejandro.prototipotf.customInterfaces;

public interface IShapeCreator {
    float getOriginalZoom();
    void addOnMatrixViewChangeListener(IOnMatrixViewChangeListener listener);
    void removeOnMatrixViewChangeListener(IOnMatrixViewChangeListener listener);
}
