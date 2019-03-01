package barsotti.alejandro.prototipotf.customViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import barsotti.alejandro.prototipotf.utils.MathUtils;
import barsotti.alejandro.prototipotf.utils.ViewUtils;
import barsotti.alejandro.prototipotf.customInterfaces.IOnMatrixViewChangeListener;
import barsotti.alejandro.prototipotf.customInterfaces.IShapeCreator;

public abstract class Shape extends View implements IOnMatrixViewChangeListener {
    //region Constantes
    /**
     * Tag utilizado a efectos de debug.
     */
    private static final String TAG = "Shape";
    /**
     * Mínimo factor de escala permitido para la matriz de la View a la que pertenece la figura.
     */
    protected static final int MIN_SCALE_FACTOR = ViewUtils.MIN_SCALE_FACTOR;
    /**
     * Máximo factor de escala permitido para la matriz de la View a la que pertenece la figura.
     */
    protected static final int MAX_SCALE_FACTOR = ViewUtils.MAX_SCALE_FACTOR;
    /**
     * Radio del círculo que representa el centro de un punto de la figura.
     */
    protected static final int CENTER_POINT_RADIUS = 2;
    /**
     * Radio de tolerancia de distancia para determinar si se realizó un toque sobre un elemento.
     */
    protected static final float TOUCH_TOLERANCE = 75;
    /**
     * Valor inicial para el radio de dibujo del círculo que representa el área de control de un punto de la
     * figura.
     */
    protected static final float POINT_RADIUS = 30;
    //endregion

    //region Propiedades
    //region Pinturas
    /**
     * Pintura utilizada para el trazo de la figura.
     */
    protected Paint mShapePaint = new Paint();
    /**
     * Pintura utilizada para el trazo de la figura cuando la misma se encuentra seleccionada.
     */
    protected Paint mSelectedShapePaint = new Paint();
    /**
     * Pintura utilizada para el borde del trazo de la figura.
     */
    protected Paint mShapeBorderPaint = new Paint();
    /**
     * Pintura utilizada para el borde del trazo de la figura cuando la misma se encuentra seleccionada.
     */
    protected Paint mSelectedShapeBorderPaint = new Paint();
    /**
     * Pintura utilizada para el relleno del círculo que representa el área tocable de un punto de la figura.
     */
    protected Paint mPointPaint = new Paint();
    /**
     * Pintura utilizada para el borde del círculo que representa el área tocable de un punto de la figura.
     */
    protected Paint mPointBorderPaint = new Paint();
    /**
     * Pintura utilizada para el punto central del círculo que representa el área tocable de un punto de la
     * figura.
     */
    protected Paint mCenterPointPaint = new Paint();
    //endregion
    /**
     * Determina si los parámetros de la figura deben calcularse constantemente al desplazar uno de los
     * puntos que la forman.
     */
    protected boolean mComputeShapeConstantly = false;
    /**
     * Número que indica el índice (posición en la lista) del punto de la figura que fue seleccionado.
     */
    protected Integer mSelectedPointIndex;
    /**
     * Variable que determina si la figura se encuentra seleccionada actualmente.
     */
    protected boolean mIsSelected = false;
    /**
     * Lista de puntos de la figura.
     */
    protected ArrayList<PointF> mShapePoints = new ArrayList<>();
    /**
     * Lista de puntos de la figura mapeados según la matriz de desplazamiento actual (mCurrentMatrix).
     */
    protected ArrayList<PointF> mMappedShapePoints = new ArrayList<>();
    /**
     * Matriz de desplazamiento actual.
     */
    protected Matrix mCurrentMatrix = new Matrix();
    /**
     * Zoom actual de la imagen.
     */
    protected float mCurrentZoom = 0;
    /**
     * Zoom aplicado sobre la imagen original al ajustarla al espacio disponible en la pantalla.
     */
    protected float mOriginalZoom = 0;
    /**
     * Radio de dibujo del círculo que representa un punto de la figura.
     */
    protected float mPointRadius = POINT_RADIUS;
    /**
     * Límite inferior para el valor del radio de dibujo del círculo que representa un punto de la figura.
     */
    protected float mPointRadiusMinLimit = 0;
    /**
     * Límite superior para el valor del radio de dibujo del círculo que representa un punto de la figura.
     */
    protected float mPointRadiusMaxLimit = 0;
    /**
     * Número que indica la anchura del espacio destinado para la View, expresado en píxeles.
     */
    protected float mViewWidth;
    /**
     * Número que indica la altura del espacio destinado para la View, expresado en píxeles.
     */
    protected float mViewHeight;
    /**
     * Detector de gestos utilizado para detectar toques, scroll, etc.
     */
    private GestureDetector mGestureDetector = new GestureDetector(getContext(), new ShapeGestureListener());
    //endregion

    //region Constructors
    public Shape(Context context) {
        this(context, null);
    }

    public Shape(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "Constructor.");

        // Inicializar pinturas utilizadas para la representanción gráfica de la figura.
        int shapeColor = getShapeColor();
        mShapePaint.setColor(shapeColor);
        mShapePaint.setStyle(Paint.Style.STROKE);
        mShapePaint.setStrokeWidth(4);

        mSelectedShapePaint.set(mShapePaint);
        mSelectedShapePaint.setAlpha(127);

        mShapeBorderPaint.setColor(Color.BLACK);
        mShapeBorderPaint.setStrokeWidth(8);
        mShapeBorderPaint.setStyle(Paint.Style.STROKE);

        mSelectedShapeBorderPaint.set(mShapeBorderPaint);
        mSelectedShapeBorderPaint.setAlpha(127);

        mPointPaint.setColor(Color.YELLOW);
        mPointPaint.setStyle(Paint.Style.FILL);
        mPointPaint.setStrokeWidth(20);
        mPointPaint.setAlpha(63);

        mCenterPointPaint.set(mShapeBorderPaint);
        mCenterPointPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mCenterPointPaint.setStrokeWidth(2);
        mCenterPointPaint.setAlpha(127);

        mPointBorderPaint.set(mSelectedShapeBorderPaint);
        mPointBorderPaint.setStrokeWidth(2);
    }
    //endregion

    //region Abstract Methods

    /**
     * Inicializa variables relevantes para la figura.
     */
    protected abstract void initializeShape();

    /**
     * Obtiene la cantidad de puntos que conforman la figura.
     * @return Número que indica la cantidad de puntos que conforman la figura.
     */
    protected abstract int getNumberOfPointsInShape();

    /**
     * Obtiene el color de la pintura utilizada para representar la figura.
     * @return Número que indica el color de la pintura utilizada para representar la figura.
     */
    protected abstract int getShapeColor();

    /**
     * Calcula la distancia entre un toque en pantalla (expresado por coordenadas de un punto) y la figura.
     * @param point Punto que indica las coordenadas del toque efectuado.
     * @return -1 en caso de que la figura no se encuentre completa o la distancia sea mayor a la
     * tolerancia definida (TOUCH_TOLERANCE). En caso contrario, retorna la distancia calculada entre el
     * punto y la figura.
     */
    public abstract float computeDistanceBetweenTouchAndShape(PointF point);

    /**
     * Realiza los cálculos y establece los valores necesarios para representar la figura.
     */
    protected abstract void computeShape();
    //endregion

    //region Utilities
    /**
     * Mapea un punto de acuerdo a una matriz dada.
     * @param matrix Matriz mediante la cual mapear el punto.
     * @param point Punto a mapear.
     * @return Punto mapeado según la matriz especificada.
     */
    protected PointF mapPoint(Matrix matrix, PointF point) {
        if (point == null) {
            return null;
        }

        // Crear Array con el punto, estructura necesaria para utilizar mapPoints.
        float[] floats = { point.x, point.y };

        // Mapear el punto.
        matrix.mapPoints(floats);

        // Crear punto con el resultado del mapeo.
        return new PointF(floats[0], floats[1]);
    }

    /**
     * Mapea una lista de puntos de acuerdo a una matriz dada.
     * @param matrix Matriz mediante la cual mapear los puntos.
     * @param pointsToMap Lista de puntos a mapear.
     * @return Lista de puntos mapeados según la matriz especificada.
     */
    protected ArrayList<PointF> mapPoints(Matrix matrix, ArrayList<PointF> pointsToMap) {
        // Crear Array con puntos, estructura necesaria para utilizar mapPoints.
        float[] pointsArray = new float[pointsToMap.size() * 2];
        for (int i = 0; i < pointsToMap.size(); i++) {
            PointF point = pointsToMap.get(i);
            pointsArray[i * 2] = point.x;
            pointsArray[i * 2 + 1] = point.y;
        }

        // Mapear los puntos.
        matrix.mapPoints(pointsArray);

        // Crear ArrayList resultado con los puntos mapeados.
        ArrayList<PointF> mappedPoints = new ArrayList<>();
        for (int i = 0; i < pointsToMap.size(); i++) {
            mappedPoints.add(new PointF(pointsArray[i * 2], pointsArray[i * 2 + 1]));
        }

        return mappedPoints;
    }
    //endregion

    @Override
    protected void onDraw(Canvas canvas) {
        // Dibujar puntos solo si la figura está seleccionada.
        if (mIsSelected) {
            for (PointF pointToDraw: mMappedShapePoints) {
                // Dibujar relleno del área de control del punto.
                canvas.drawCircle(pointToDraw.x, pointToDraw.y, mPointRadius, mPointPaint);
                // Dibujar borde del área de control del punto.
                canvas.drawCircle(pointToDraw.x, pointToDraw.y, mPointRadius, mPointBorderPaint);
                // Dibujar punto central del área de control del punto.
                canvas.drawCircle(pointToDraw.x, pointToDraw.y, CENTER_POINT_RADIUS, mCenterPointPaint);
            }
        }
    }

    @Override
    public void updateViewMatrix(Matrix matrix) {
        // Inicializar las variables que regulan el tamaño del radio de tolerancia a toques de los puntos.
        if (mPointRadiusMaxLimit == 0) {
            // Radio máximo: 1/6 de la longitud del lado más largo del espacio disponible en pantalla.
            mPointRadiusMaxLimit = Math.max(mViewWidth, mViewHeight) / 6;
            // Radio mínimo: 1/18 de la longitud del lado más largo del espacio disponible en pantalla.
            mPointRadiusMinLimit = mPointRadiusMaxLimit / 3;
        }

        // Actualizar variable de la matriz actual.
        if (matrix != null) {
            mCurrentMatrix.set(matrix);
        }

        // Ajustar radio de tolerancia a toques de los puntos según el zoom actual.
        float[] floats = new float[9];
        mCurrentMatrix.getValues(floats);
        mCurrentZoom = floats[Matrix.MSCALE_X];
        float realZoom = mCurrentZoom / mOriginalZoom;
        // Calcular porcentaje del rango [MIN_SCALE_FACTOR, MAX_SCALE_FACTOR] al que equivale realZoom.
        float percentage = (realZoom - MIN_SCALE_FACTOR) / (MAX_SCALE_FACTOR - MIN_SCALE_FACTOR);
        mPointRadius = mPointRadiusMinLimit + (mPointRadiusMaxLimit - mPointRadiusMinLimit) * percentage;
    }

    @Override
    public void setViewMeasures(float width, float height) {
        mViewWidth = width;
        mViewHeight = height;
    }

    /**
     * Agrega un nuevo punto a la forma.
     * @param point Punto a agregar a la lista de puntos de la forma.
     * @return True si la figura aún no fue completada. False en caso contrario.
     */
    public boolean addPoint(PointF point) {
        // Validar que sea posible agregar un nuevo punto a la figura.
        if (mShapePoints.size() < getNumberOfPointsInShape()) {
            mShapePoints.add(point);

            // Calcular variables de la figura en caso de que la misma haya sido completada.
            if (mShapePoints.size() == getNumberOfPointsInShape()) {
                computeShape();
            }

            // Requerir redibujo para mostrar el nuevo punto.
            invalidate();
        }

        return mShapePoints.size() < getNumberOfPointsInShape();
    }

    /**
     * Cambia el estado de selección de la figura.
     * @param isSelected Determina si la figura debe seleccionarse (True) o deseleccionarse (False).
     */
    public void selectShape(boolean isSelected) {
        mIsSelected = isSelected;

        // Requerir redibujo para mostrar gráficamente el estado de la selección.
        invalidate();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Verificar si el evento debe procesarse. Solo se analizará cuando la figura esté seleccionada.
        if (!mIsSelected) {
            return false;
        }

        // Procesar evento con el detector de gestos.
        boolean gestureDetectorResponse = mGestureDetector.onTouchEvent(event);

        // Determinar si finalizó el desplazamiento de un punto para calcular nuevamente la forma.
        if (event.getAction() == MotionEvent.ACTION_UP) {
            // Calcular solo si la forma se encuentra completa.
            if (mShapePoints.size() == getNumberOfPointsInShape()) {
                computeShape();
            }
            updateViewMatrix(null);
        }

        return gestureDetectorResponse;
    }

    /**
     * Clase que define el comportamiento del objeto ante los gestos definidos.
     */
    private class ShapeGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            /*
            Verificar que el toque se haya producido en las inmediaciones de uno de los puntos de la figura.
            De no ser así, no capturar el evento.
             */
            float eX = e.getX();
            float eY = e.getY();
            for (int i = 0; i < mMappedShapePoints.size(); i++) {
                PointF point = mMappedShapePoints.get(i);
                if (MathUtils.computeDistanceBetweenPoints(eX, eY, point.x, point.y) <= mPointRadius) {
                    mSelectedPointIndex = i;
                    return true;
                }
            }

            mSelectedPointIndex = null;

            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Si un punto está seleccionado, desplazarlo según el scroll detectado.
            if (mSelectedPointIndex != null) {
                mShapePoints.get(mSelectedPointIndex).offset(-distanceX / mCurrentZoom,
                    -distanceY / mCurrentZoom);

                // Calcular parámetros de la figura si fue configurada para calcular constantemente.
                if (mComputeShapeConstantly) {
                    computeShape();
                }

                updateViewMatrix(null);

                return true;
            }

            return false;
        }
    }

    //region Administración de suscripciones
    /**
     * Suscribe la figura a las actualizaciones de la matriz del objeto que la creó.
     * @param shapeCreator Objeto que creó la figura.
     */
    public void addShapeCreatorListener(IShapeCreator shapeCreator) {
        // Obtener zoom original aplicado sobre la imagen que muestra el objeto que creó la figura.
        mOriginalZoom = shapeCreator.getOriginalZoom();

        shapeCreator.addOnMatrixViewChangeListener(this);
    }

    /**
     * Da de baja la suscripción de la figura a las actualizaciones de la matriz del objeto que la creó.
     * @param shapeCreator Objeto que creó la figura.
     */
    public void removeShapeCreatorListener(IShapeCreator shapeCreator) {
        shapeCreator.removeOnMatrixViewChangeListener(this);
    }
    //endregion
}
