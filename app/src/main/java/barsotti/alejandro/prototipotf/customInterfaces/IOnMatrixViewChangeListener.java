package barsotti.alejandro.prototipotf.customInterfaces;

import android.graphics.Matrix;

public interface IOnMatrixViewChangeListener {
    /**
     * Realiza los cálculos necesarios para ajustar la representación del observador luego de un cambio en la
     * matriz de representación utilizada por el objeto observable.
     * @param matrix Nueva matriz de representación utilizada por el objeto observable.
     */
    void updateViewMatrix(Matrix matrix);

    /**
     * Establece los valores de ancho y alto del espacio ocupado por el objeto observable. Esto sirve para
     * que el observador pueda ajustar diversos parámetros de su representación en función.
     * @param width Ancho del espacio ocupado por el objeto observable.
     * @param height Alto del espacio ocupado por el objeto observable.
     */
    void setViewMeasures(float width, float height);
}
