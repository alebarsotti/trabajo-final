package barsotti.alejandro.prototipotf.customInterfaces;

import android.graphics.PointF;

import barsotti.alejandro.prototipotf.customViews.Circle;

/**
 * Interfaz para aquellos objetos que dependen de actualizaciones de un objeto {@link Circle}.
 */
public interface IOnCircleCenterChangeListener {
    /**
     * Actualiza los valores del centro y el radio del {@link Circle} para los suscriptores.
     * @param center Nuevo valor del centro del {@link Circle}.
     * @param radius Nuevo valor del radio del {@link Circle}.
     */
    void updateCircleCenterAndRadius(PointF center, float radius);
}
