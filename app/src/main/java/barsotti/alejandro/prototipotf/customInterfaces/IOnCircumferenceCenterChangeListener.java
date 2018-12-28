package barsotti.alejandro.prototipotf.customInterfaces;

import android.graphics.PointF;

import barsotti.alejandro.prototipotf.customViews.Circumference;

/**
 * Interfaz para aquellos objetos que dependen de actualizaciones de un objeto {@link Circumference}.
 */
public interface IOnCircumferenceCenterChangeListener {
    /**
     * Actualiza los valores del centro y el radio de {@link Circumference} para los suscriptores.
     * @param center Nuevo valor del centro de {@link Circumference}.
     * @param radius Nuevo valor del radio de {@link Circumference}.
     */
    void updateCircumferenceCenterAndRadius(PointF center, float radius);
}
