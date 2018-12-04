package barsotti.alejandro.prototipotf.customViews;

/**
 * Interfaz utilizada por {@link Circle} para el tratamiento de suscriptores a eventos de actualización.
 */
public interface ICircle {
    /**
     * Agrega un nuevo listener de los eventos de actualización del objeto.
     * @param listener Nuevo objeto suscriptor a agregar a la lista de suscriptores.
     */
    void addOnCircleCenterChangeListener(IOnCircleCenterChangeListener listener);

    /**
     * Elimina un listener de los eventos de actualización del objeto.
     * @param listener Objeto suscriptor a eliminar de la lista de suscriptores.
     */
    void removeOnCircleCenterChangeListener(IOnCircleCenterChangeListener listener);
}
