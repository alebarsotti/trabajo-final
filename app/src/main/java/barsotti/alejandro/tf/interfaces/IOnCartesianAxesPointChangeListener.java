package barsotti.alejandro.tf.interfaces;

import android.graphics.PointF;
import barsotti.alejandro.tf.views.CartesianAxes;

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