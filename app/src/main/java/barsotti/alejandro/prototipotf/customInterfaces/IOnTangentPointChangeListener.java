package barsotti.alejandro.prototipotf.customInterfaces;

import android.graphics.PointF;
import barsotti.alejandro.prototipotf.customViews.Tangent;

/**
 * Interfaz para aquellos objetos que dependen de actualizaciones de un objeto {@link Tangent}.
 */
public interface IOnTangentPointChangeListener {
    /**
     * Actualiza el valor de los puntos del objeto {@link Tangent} para los suscriptores.
     * @param tangentPoint Nuevo valor del punto de la tangente del objeto {@link Tangent}.
     * @param linePoint Nuevo valor del punto de la línea tangente del objeto {@link Tangent}.
     * @param circleCenterPoint Nuevo valor del punto del centro de la circunferencia de referencia.
     */
    void updateTangentPoints(PointF tangentPoint, PointF linePoint, PointF circleCenterPoint);
}