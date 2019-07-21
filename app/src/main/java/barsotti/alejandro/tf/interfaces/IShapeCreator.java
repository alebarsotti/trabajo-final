package barsotti.alejandro.tf.interfaces;

import barsotti.alejandro.tf.views.Shape;

/**
 * Interfaz que deben implementar los objetos que deseen crear nuevas instancias de objetos derivados de
 * {@link Shape}.
 */
public interface IShapeCreator {
    /**
     * Obtiene el valor del zoom original aplicado a la imagen representada por el objeto.
     * @return Zoom original aplicado.
     */
    float getOriginalZoom();

    /**
     * Agrega un nuevo listener de los eventos de actualización del objeto.
     * @param listener Nuevo objeto suscriptor a agregar a la lista de suscriptores.
     */
    void addOnMatrixViewChangeListener(IOnMatrixViewChangeListener listener);

    /**
     * Agrega un nuevo listener de los eventos de actualización del objeto.
     * @param listener Nuevo objeto suscriptor a agregar a la lista de suscriptores.
     */
    void removeOnMatrixViewChangeListener(IOnMatrixViewChangeListener listener);
}
