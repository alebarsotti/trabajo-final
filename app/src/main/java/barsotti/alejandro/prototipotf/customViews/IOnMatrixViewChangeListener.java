package barsotti.alejandro.prototipotf.customViews;

import android.graphics.Matrix;

public interface IOnMatrixViewChangeListener {
    /**
     * Realiza los cálculos necesarios para ajustar la representación del observador luego de un cambio en la
     * matriz de representación utilizada por el objeto observable.
     * @param matrix Nueva matriz de representación utilizada por el objeto observable.
     */
    void updateViewMatrix(Matrix matrix);
}
