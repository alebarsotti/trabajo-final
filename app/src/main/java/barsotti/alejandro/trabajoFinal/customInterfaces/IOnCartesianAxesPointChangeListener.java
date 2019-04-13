package barsotti.alejandro.trabajoFinal.customInterfaces;

import android.graphics.PointF;
import barsotti.alejandro.trabajoFinal.customViews.CartesianAxes;

/**
 * Interfaz para aquellos objetos que dependen de actualizaciones de un objeto {@link CartesianAxes}.
 */
public interface IOnCartesianAxesPointChangeListener {
    /**
     * Actualiza los valores de los puntos del objeto {@link CartesianAxes} para los suscriptores.
     * @param cartesianAxesPoints Lista de puntos que definen los Ejes Cartesianos.
     * @param cartesianAxesOrigin Punto en que se intersecan los ejes.
     */
    void updateCartesianAxesPoints(float[] cartesianAxesPoints, PointF cartesianAxesOrigin);
}