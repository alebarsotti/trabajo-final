package barsotti.alejandro.tf.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Locale;

import barsotti.alejandro.tf.R;
import barsotti.alejandro.tf.interfaces.IOnCartesianAxesPointChangeListener;
import barsotti.alejandro.tf.interfaces.IOnCircumferenceCenterChangeListener;
import barsotti.alejandro.tf.interfaces.IOnToothPitchChangeListener;

import static barsotti.alejandro.tf.views.Shape.TOUCH_TOLERANCE;

public class ZoomableImageViewGroup extends FrameLayout {

    //region Constantes
    /**
     * Tag utilizado con fines de debug.
     */
    private static final String TAG = "ZoomableImageViewGroup";
    //endregion

    //region Propiedades
    /**
     * View encargada de la representación gráfica de la imagen y las figuras dibujadas.
     */
    private ZoomableImageView mZoomableImageView;
    /**
     * Lista de figuras dibujadas.
     */
    private ArrayList<Shape> mShapeList = new ArrayList<>();
    /**
     * Objeto figura que se está dibujando actualmente.
     */
    private Shape mInProgressShape;
    /**
     * Objeto figura que está seleccionado actualmente.
     */
    private Shape mCurrentlySelectedShape;
    private Locale locale = new Locale("es", "ES");
    /**
     * Número que indica el índice de color a utilizar para el próximo ángulo a crear.
     */
    private int AngleColorIndex = 0;
    private int[] AngleColorList = new int[] {Color.GREEN, Color.MAGENTA, Color.RED, Color.BLUE};
    //endregion

    // region Constructors
    public ZoomableImageViewGroup(@NonNull Context context) {
        this(context, null);
    }

    public ZoomableImageViewGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        Log.d(TAG, getContext().getResources().getString(R.string.constructor_log_message));

        // Inicializar ViewGroup.
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.zoomable_image_view_group, this, true);
        mZoomableImageView = view.findViewById(R.id.zoomable_image_view);
    }
    // endregion

    /**
     * Establece el estado de la View encargada de la representación gráfica.
     * @param viewState Nuevo estado de la View.
     */
    public void setZoomableImageViewState(ZoomableImageView.ViewState viewState) {
        mZoomableImageView.setState(viewState);
    }

    /**
     * Establece el estado actual de dibujo en progreso y permite inicializar el dibujo de una nueva figura.
     * @param drawingInProgress Nuevo estado de dibujo en progreso.
     * @param shapeClass Clase de figura a dibujar.
     */
    public void setZoomableImageViewDrawingInProgress(boolean drawingInProgress, Class shapeClass) {
        if (!drawingInProgress) {
            mInProgressShape = null;
        }
        else {
            try {
                // Eliminar la figura que se estaba dibujando previamente, en caso de existir una.
                abortPreviousDrawing();

                // Verificar que la figura especificada pueda dibujarse.
                checkCanDrawShape(shapeClass);

                // Instanciar e inicializar la nueva figura.
                initializeFigure(shapeClass);

                // Verificar reglas especiales de inicialización.
                handleSpecialInitializationRules(shapeClass);
            } catch (Exception e) {
                Log.e(TAG, "setZoomableImageViewDrawingInProgress:");
                e.printStackTrace();
                drawingInProgress = false;
            }
        }
        mZoomableImageView.setDrawingInProgress(drawingInProgress);
    }

    private void initializeFigure(Class shapeClass) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, java.lang.reflect.InvocationTargetException {
        Class<?> myClassType = Class.forName(shapeClass.getName());
        Class<?>[] types = new Class[] { Context.class };
        Constructor<?> cons = myClassType.getConstructor(types);
        mInProgressShape = (Shape) cons.newInstance(getContext());
        mInProgressShape.addShapeCreatorListener(mZoomableImageView);
        mInProgressShape.selectShape(true);
        for (Shape shape: mShapeList) {
            shape.selectShape(false);
        }
        mShapeList.add(mInProgressShape);
        this.addView(mInProgressShape);
    }

    private void handleSpecialInitializationRules(Class shapeClass) {
        // Tratar especialmente la inicialización de la figura para el caso de los Ángulos.
        if (shapeClass == Angle.class) {
            for (Shape shape : mShapeList) {
                if (shape.getClass() == CartesianAxes.class) {
                    ((CartesianAxes)shape).addOnCartesianAxesPointChangeListener(
                        (IOnCartesianAxesPointChangeListener) mInProgressShape);
                    break;
                }
            }
            // Establecer color del ángulo.
            ((Angle) mInProgressShape).setShapeColor(AngleColorList[AngleColorIndex]);
            AngleColorIndex++;
            if (AngleColorIndex == AngleColorList.length) {
                AngleColorIndex = 0;
            }
        }
        // Tratar especialmente la inicialización de la figura para el caso de la Diferencia HZ.
        else if (shapeClass == DifferenceHZ.class) {
            for (Shape shape : mShapeList) {
                if (shape.getClass() == Circumference.class) {
                    ((Circumference) shape).addOnCircumferenceCenterChangeListener(
                        (IOnCircumferenceCenterChangeListener) mInProgressShape);
                    break;
                }
            }
            for (Shape shape : mShapeList) {
                if (shape.getClass() == ToothPitch.class) {
                    ((ToothPitch)shape).addOnToothPitchChangeListener(
                        (IOnToothPitchChangeListener) mInProgressShape);
                    break;
                }
            }
        }
    }

    private void abortPreviousDrawing() {
        if (mInProgressShape != null) {
            // Dar de baja la suscripción a las actualizaciones de la matriz del objeto creador.
            mInProgressShape.removeShapeCreatorListener(mZoomableImageView);

            // Deseleccionar todas las figuras.
            for (Shape shape: mShapeList) {
                shape.selectShape(false);
            }

            // Eliminar forma de la lista de formas.
            mShapeList.remove(mInProgressShape);

            // Eliminar view de la forma.
            this.removeView(mInProgressShape);
        }
    }

    /**
     * Verifica que sea posible dibujar la figura especificada.
     * @param shapeClass Figura a dibujar.
     * @throws Exception En caso de que no sea posible dibujar la figura especificada.
     */
    private void checkCanDrawShape(Class shapeClass) throws Exception {
        // Caso: Ángulo. No es posible dibujar un ángulo si no existen Ejes Cartesianos previamente.
        if (shapeClass == Angle.class) {
            boolean canDrawAngle = false;
            for (Shape shape: mShapeList) {
                if (shape.getClass() == CartesianAxes.class) {
                    canDrawAngle = true;
                    break;
                }
            }

            if (!canDrawAngle) {
                Toast.makeText(getContext(), R.string.cannot_draw_angle_message,
                    Toast.LENGTH_SHORT).show();
                throw new Exception();
            }
        }
        // Caso: Diferencia HZ. No es posible dibujarla si no existe una circunferencia y un paso definido.
        if (shapeClass == DifferenceHZ.class) {
            boolean circumferenceExists = false;
            boolean toothPitchExists = false;
            for (Shape shape: mShapeList) {
                if (shape.getClass() == Circumference.class) {
                    circumferenceExists = true;
                } else if (shape.getClass() == ToothPitch.class) {
                    toothPitchExists = true;
                }
            }

            if (!circumferenceExists || !toothPitchExists) {
                Toast.makeText(getContext(), R.string.cannot_draw_hz_message,
                    Toast.LENGTH_SHORT).show();
                throw new Exception();
            }
        }

    }

    /**
     * Inicializa y configura la View encargada de la representación gráfica.
     * @param displayWidth Anchura de la pantalla del dispositivo.
     * @param displayHeight Altura de la pantalla del dispositivo.
     * @param uri Uri de la imagen original a mostrar.
     * @param drawable Imagen escalada generada por Glide.
     */
    public void setupZoomableImageView(int displayWidth, int displayHeight, Uri uri, Drawable drawable) {
        mZoomableImageView.setupZoomableImageView(displayWidth, displayHeight, uri, drawable);
    }

    /**
     * Agrega un nuevo punto a la figura que se encuentra en proceso de dibujo.
     * @param point Punto a agregar a la figura.
     */
    public void addPointToInProgressShape(PointF point) {
        // Verificar que exista una figura en proceso.
        if (mInProgressShape != null) {
            // Agregar punto a la figura y determinar si el dibujo fue finalizado.
            boolean finishedShape = !mInProgressShape.addPoint(point);

            // Informar finalización del dibujo en progreso en caso de haber completado la figura.
            if (finishedShape) {
                setZoomableImageViewDrawingInProgress(false, null);
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * Verifica si un toque seleccionó alguna figura dibujada.
     * @param point Coordenadas del toque en pantalla.
     */
    public void checkShapeSelection(PointF point) {
        float minDistance = TOUCH_TOLERANCE + 1;
        boolean minDistanceShapeIsAngle = false;

        /*
        Recorrer la lista de figuras para determinar cuál se encuentra a menor distancia del toque efectuado.
        Priorizar la selección de ángulos para mejorar la usabilidad.
         */
        for (int i = mShapeList.size() - 1; i >= 0; i--) {
            boolean newShapeIsAngle = mShapeList.get(i).getClass() == Angle.class;
            float newDistance = mShapeList.get(i).computeDistanceBetweenTouchAndShape(point);
            if (newDistance > 0 && (
                    (!minDistanceShapeIsAngle && (newShapeIsAngle || newDistance < minDistance)) ||
                    (minDistanceShapeIsAngle && newShapeIsAngle && newDistance < minDistance))
                ) {
                minDistanceShapeIsAngle = newShapeIsAngle;
                minDistance = newDistance;
                mCurrentlySelectedShape = mShapeList.get(i);
            }
        }

        /*
        Si la figura más cercana al toque se encuentra a una distancia mayor a la tolerancia definida,
        no seleccionar ninguna figura.
         */
        if (minDistance > TOUCH_TOLERANCE) {
            mCurrentlySelectedShape = null;
        }

        // Deseleccionar todas las formas excepto la seleccionada (en caso de que haya).
        for (Shape shape: mShapeList) {
            shape.selectShape(shape == mCurrentlySelectedShape);
        }
    }

    public String getAngleMeasures() {
        StringBuilder angleMeasures = new StringBuilder();
        int angleIndex = 0;

        for (Shape shape: mShapeList) {
            if (shape.getClass() == Angle.class) {
                float angleMeasure = ((Angle) shape).getSweepAngleMeasure();
                angleIndex++;
                angleMeasures.append(String.format(locale, "Ángulo %s: %.2fº\n", angleIndex, angleMeasure));
            }
        }

        return angleMeasures.toString();
    }
}
