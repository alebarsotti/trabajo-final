package barsotti.alejandro.tf.interfaces;

import android.graphics.PointF;

import barsotti.alejandro.tf.views.Circumference;

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
