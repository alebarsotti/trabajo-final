package barsotti.alejandro.prototipotf.customInterfaces;

import barsotti.alejandro.prototipotf.customViews.Tangent;

/**
 * Interfaz utilizada por {@link Tangent} para el tratamiento de suscriptores a eventos de actualización.
 */
public interface ITangent {
    /**
     * Agrega un nuevo listener de los eventos de actualización del objeto.
     * @param listener Nuevo objeto suscriptor a agregar a la lista de suscriptores.
     */
    void addOnTangentPointChangeListener(IOnTangentPointChangeListener listener);

    /**
     * Elimina un listener de los eventos de actualización del objeto.
     * @param listener Objeto suscriptor a eliminar de la lista de suscriptores.
     */
    void removeOnTangentPointChangeListener(IOnTangentPointChangeListener listener);
}
