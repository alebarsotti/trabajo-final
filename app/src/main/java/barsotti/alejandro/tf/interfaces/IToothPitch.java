package barsotti.alejandro.tf.interfaces;

import barsotti.alejandro.tf.views.ToothPitch;

/**
 * Interfaz utilizada por {@link ToothPitch} para el tratamiento de suscriptores a eventos de actualización.
 */
public interface IToothPitch {
    /**
     * Agrega un nuevo listener de los eventos de actualización del objeto.
     * @param listener Nuevo objeto suscriptor a agregar a la lista de suscriptores.
     */
    void addOnToothPitchChangeListener(IOnToothPitchChangeListener listener);

    /**
     * Elimina un listener de los eventos de actualización del objeto.
     * @param listener Objeto suscriptor a eliminar de la lista de suscriptores.
     */
    void removeOnToothPitchChangeListener(IOnToothPitchChangeListener listener);
}
