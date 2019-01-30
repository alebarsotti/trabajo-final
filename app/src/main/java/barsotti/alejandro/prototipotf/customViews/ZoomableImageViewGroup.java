package barsotti.alejandro.prototipotf.customViews;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import barsotti.alejandro.prototipotf.R;
import barsotti.alejandro.prototipotf.utils.MathUtils;
import barsotti.alejandro.prototipotf.customInterfaces.IOnTangentPointChangeListener;

import static barsotti.alejandro.prototipotf.customViews.Shape.TOUCH_TOLERANCE;

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

                // Verificar que la figura especificada pueda dibujarse.
                checkCanDrawShape(shapeClass);

                // Instanciar e inicializar la nueva figura.
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

                // Tratar especialmente la inicialización de la figura para el caso de los Ángulos.
                if (shapeClass == Angle.class) {
                    for (Shape shape : mShapeList) {
                        if (shape.getClass() == Tangent.class) {
                            ((Tangent)shape).addOnTangentPointChangeListener(
                                (IOnTangentPointChangeListener) mInProgressShape);
                            break;
                        }
                    }

                    addPointToInProgressShape(new PointF(0, 0));
                    drawingInProgress = false;
                }
            } catch (Exception e) {
                Log.e(TAG, "setZoomableImageViewDrawingInProgress:");
                e.printStackTrace();
                drawingInProgress = false;
            }
        }
        mZoomableImageView.setDrawingInProgress(drawingInProgress);
    }

    /**
     * Verifica que sea posible dibujar la figura especificada.
     * @param shapeClass Figura a dibujar.
     * @throws Exception En caso de que no sea posible dibujar la figura especificada.
     */
    private void checkCanDrawShape(Class shapeClass) throws Exception {
        // Caso: Tangente. No es posible dibujar una tangente si no existe una circunferencia previamente.
        if (shapeClass == Tangent.class) {
            boolean canDrawTangent = false;
            for (Shape shape: mShapeList) {
                if (shape.getClass() == Circumference.class) {
                    canDrawTangent = true;
                    break;
                }
            }

            if (!canDrawTangent) {
                Toast.makeText(getContext(), R.string.cannot_draw_tangent_message,
                    Toast.LENGTH_SHORT).show();
                throw new Exception();
            }
        }

        // Caso: Ángulo. No es posible dibujar un ángulo si no existe una tangente previamente.
        if (shapeClass == Angle.class) {
            boolean canDrawAngle = false;
            for (Shape shape: mShapeList) {
                if (shape.getClass() == Tangent.class) {
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
            // Caso: Tangente. Determinar a qué circunferencia corresponde asociar la tangente.
            if (mInProgressShape.getClass() == Tangent.class) {
                Circumference closestCircumference = null;
                double distance = -1;
                for (Shape shape : mShapeList) {
                    if (shape.getClass() == Circumference.class) {
                        PointF circleCenter = ((Circumference) shape).mMappedCenter;
                        double newDistance = Math.abs(MathUtils.computeDistanceBetweenPoints(point.x, point.y,
                            circleCenter.x, circleCenter.y) - ((Circumference) shape).mMappedRadius);
                        if (distance < 0 || newDistance < distance) {
                            distance = newDistance;
                            closestCircumference = (Circumference) shape;
                        }
                    }
                }

                if (closestCircumference != null) {
                    closestCircumference.addOnCircumferenceCenterChangeListener((Tangent) mInProgressShape);
                }
            }

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
}
