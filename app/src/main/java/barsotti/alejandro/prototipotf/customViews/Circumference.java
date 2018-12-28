package barsotti.alejandro.prototipotf.customViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;

import barsotti.alejandro.prototipotf.Utils.MathUtils;
import barsotti.alejandro.prototipotf.customInterfaces.ICircumference;
import barsotti.alejandro.prototipotf.customInterfaces.IOnCircumferenceCenterChangeListener;

public class Circumference extends Shape implements ICircumference {
    //region Constantes
    /**
     * Tag utilizado con fines de debug.
     */
    private static final String TAG = "Circumference";
    /**
     * Número que indica la cantidad de puntos que componen la circunferencia.
     */
    private static final int NUMBER_OF_POINTS = 3;
    /**
     * Número que indica la cantidad de puntos a utilizar para representar gráficamente la circunferencia.
     */
    public static final int NUMBER_OF_POINTS_TO_DRAW = 360;
    /**
     * Color utilizado para la representación gráfica de la circunferencia.
     */
    private static final int SHAPE_COLOR = Color.RED;
    //endregion

    //region Propiedades
    /**
     * Punto que indica las coordenadas del centro de la circunferencia.
     */
    private PointF mCenter;
    /**
     * Número que indica la longitud del radio de la circunferencia.
     */
    private float mRadius;
    /**
     * Punto que indica las coordenadas del centro de la circunferencia mapeado según la matriz actual.
     */
    public PointF mMappedCenter;
    /**
     * Número que indica la longitud del radio de la circunferencia mapeado según la matriz actual.
     */
    public float mMappedRadius;
    /**
     * Lista de puntos (en formato [x1, y1, x2, y2, ...]) utilizados para representar gráficamente la
     * circunferencia.
     */
    private float[] mPathPoints = new float[NUMBER_OF_POINTS_TO_DRAW * 4];
    /**
     * Lista de puntos (en formato [x1, y1, x2, y2, ...]) utilizados para representar gráficamente la
     * circunferencia mapeados según la matriz actual.
     */
    private float[] mMappedPathPoints = new float[NUMBER_OF_POINTS_TO_DRAW * 4];
    /**
     * Lista de suscriptores a eventos de actualización de la circunferencia.
     */
    private ArrayList<IOnCircumferenceCenterChangeListener> mListeners = new ArrayList<>();
    //endregion

    //region Constructores
    public Circumference(Context context) {
        this(context, null);
    }

    public Circumference(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initializeShape();
        Log.d(TAG, "Constructor.");
    }
    //endregion

    //region Administración de listeners
    @Override
    public void addOnCircumferenceCenterChangeListener(IOnCircumferenceCenterChangeListener listener) {
        mListeners.add(listener);
        listener.updateCircumferenceCenterAndRadius(mCenter, mRadius);
    }

    @Override
    public void removeOnCircumferenceCenterChangeListener(IOnCircumferenceCenterChangeListener listener) {
        try {
            mListeners.remove(listener);
        }
        catch (Exception e) {
            Log.d(TAG, "removeOnMatrixViewChangeListener: the object was not found in the list.");
        }
    }
    //endregion

    @Override
    protected void initializeShape() {
    }

    @Override
    protected int getNumberOfPointsInShape() {
        return NUMBER_OF_POINTS;
    }

    @Override
    protected int getShapeColor() {
        return SHAPE_COLOR;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Dibujar circunferencia si la misma se encuentra completa.
        if (mPathPoints != null) {
            // Dibujar borde.
            canvas.drawLines(mMappedPathPoints, mIsSelected ? mSelectedShapeBorderPaint : mShapeBorderPaint);
            // Dibujar línea principal.
            canvas.drawLines(mMappedPathPoints, mIsSelected ? mSelectedShapePaint : mShapePaint);
        }

        super.onDraw(canvas);
    }

    @Override
    protected void computeShape() {
        // Determinar parámetros de la circunferencia según los tres puntos proporcionados.
        MathUtils.circumferenceFromThreePoints(this);
    }

    @Override
    public void updateViewMatrix(Matrix matrix) {
        super.updateViewMatrix(matrix);

        // Mapear las variables propias de la circunferencia según la nueva matriz.
        mMappedRadius = mCurrentMatrix.mapRadius(mRadius);
        mMappedCenter = mapPoint(mCurrentMatrix, mCenter);
        mMappedShapePoints = mapPoints(mCurrentMatrix, mShapePoints);
        mCurrentMatrix.mapPoints(mMappedPathPoints, mPathPoints);

        invalidate();
    }

    @Override
    public float computeDistanceBetweenTouchAndShape(PointF point) {
        // Si la circunferencia no se encuentra completa, no medir distancia.
        if (mMappedCenter == null) {
            return -1;
        }

        // Determinar la distancia entre el punto y el centro de la circunferencia.
        float distanceFromCenterToPoint = MathUtils.distanceBetweenPoints(mMappedCenter.x, mMappedCenter.y,
            point.x, point.y);

        // La distancia del toque estará dada por el valor absoluto de la diferencia entre el radio de la
        // circunferencia y la distancia medida entre el punto y el centro de la misma, dado que la
        // distancia relevante es la existente entre su borde y el punto.
        float distanceFromCircumferenceToPoint = Math.abs(mMappedRadius - distanceFromCenterToPoint);

        // Devolver la distancia medida si la misma se encuentra dentro de la tolerancia definida.
        return distanceFromCircumferenceToPoint <= TOUCH_RADIUS ? distanceFromCircumferenceToPoint : -1;
    }

    /**
     * Establece nuevos valores para el centro y el radio de la circunferencia.
     * @param newCenter Nuevo valor para el centro de la circunferencia.
     * @param newRadius Nuevo valor para el radio de la circunferencia.
     */
    public void setCenterAndRadius(PointF newCenter, float newRadius) {
        mCenter = newCenter;
        mRadius = newRadius;

        // Informar a los suscriptores sobre la actualización de los valores de centro y radio.
        for (IOnCircumferenceCenterChangeListener listener: mListeners) {
            listener.updateCircumferenceCenterAndRadius(mCenter, mRadius);
        }
    }

    /**
     * Establece la nueva lista de puntos que representan a la circunferencia.
     * @param pointsArray Lista de puntos actualizada.
     */
    public void setPathPoints(float[] pointsArray) {
        mPathPoints = pointsArray;
    }
}