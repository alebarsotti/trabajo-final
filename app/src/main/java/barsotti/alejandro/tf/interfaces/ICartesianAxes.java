package barsotti.alejandro.tf.interfaces;

import barsotti.alejandro.tf.views.CartesianAxes;

/**
 * Interfaz utilizada por {@link CartesianAxes} para el tratamiento de suscriptores a eventos de actualización.
 */
public interface ICartesianAxes {
    /**
     * Agrega un nuevo listener de los eventos de actualización del objeto.
     * @param listener Nuevo objeto suscriptor a agregar a la lista de suscriptores.
     */
    void addOnCartesianAxesPointChangeListener(IOnCartesianAxesPointChangeListener listener);

    /**
     * Elimina un listener de los eventos de actualización del objeto.
     * @param listener Objeto suscriptor a eliminar de la lista de suscriptores.
     */
    void removeOnCartesianAxesPointChangeListener(IOnCartesianAxesPointChangeListener listener);
}
