package barsotti.alejandro.prototipotf.customInterfaces;

import barsotti.alejandro.prototipotf.customViews.Circumference;

/**
 * Interfaz utilizada por {@link Circumference} para el tratamiento de suscriptores a eventos de actualización.
 */
public interface ICircumference {
    /**
     * Agrega un nuevo listener de los eventos de actualización del objeto.
     * @param listener Nuevo objeto suscriptor a agregar a la lista de suscriptores.
     */
    void addOnCircumferenceCenterChangeListener(IOnCircumferenceCenterChangeListener listener);

    /**
     * Elimina un listener de los eventos de actualización del objeto.
     * @param listener Objeto suscriptor a eliminar de la lista de suscriptores.
     */
    void removeOnCircumferenceCenterChangeListener(IOnCircumferenceCenterChangeListener listener);
}
