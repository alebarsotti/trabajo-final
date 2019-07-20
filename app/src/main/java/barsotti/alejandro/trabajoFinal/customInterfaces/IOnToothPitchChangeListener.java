package barsotti.alejandro.trabajoFinal.customInterfaces;

import barsotti.alejandro.trabajoFinal.customViews.ToothPitch;

/**
 * Interfaz para aquellos objetos que dependen de actualizaciones de un objeto {@link ToothPitch}.
 */
public interface IOnToothPitchChangeListener {
    /**
     * Actualiza la referencia del valor de la escala de la imagen (mm por pixel) definida por el paso.
     * @param millimetersPerPixel Nuevo valor de la escala definida por el paso.
     */
    void onToothPitchValueChange(float millimetersPerPixel);
}
