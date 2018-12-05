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

public class Circle extends Shape implements ICircle {
    // Tag utilizado con fines de debug.
    private static final String TAG = "Circle";
    // Número que indica la cantidad de puntos que componen la circunferencia.
    private static final int NUMBER_OF_POINTS = 3;
    // Número que indica la cantidad de puntos a utilizar para representar gráficamente la circunferencia.
    public static final int NUMBER_OF_POINTS_TO_DRAW = 360;
    // Punto que indica las coordenadas del centro de la circunferencia.
    private PointF mCenter;
    // Número que indica la longitud del radio de la circunferencia.
    private float mRadius;
    // Punto que indica las coordenadas del centro de la circunferencia mapeado según la matriz actual.
    public PointF mMappedCenter;
    // Número que indica la longitud del radio de la circunferencia mapeado según la matriz actual.
    public float mMappedRadius;
    // Lista de puntos (en formato [x1, y1, x2, y2, ...]) utilizados para representar gráficamente la
    // circunferencia.
    private float[] mPathPoints = new float[NUMBER_OF_POINTS_TO_DRAW * 4];
    // Lista de puntos (en formato [x1, y1, x2, y2, ...]) utilizados para representar gráficamente la
    // circunferencia mapeados según la matriz actual.
    private float[] mMappedPathPoints = new float[NUMBER_OF_POINTS_TO_DRAW * 4];
    // Lista de suscriptores a eventos de actualización de la circunferencia.
    private ArrayList<IOnCircleCenterChangeListener> mListeners = new ArrayList<>();

    //region Constructores
    public Circle(Context context) {
        this(context, null);
    }

    public Circle(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initializeShape();
        Log.d(TAG, "Constructor.");
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
        return Color.RED;
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

//        // Dibujar puntos solo si la figura está seleccionada.
//        if (mIsSelected) {
//            for (PointF pointToDraw: mMappedShapePoints) {
//                // Dibujar relleno del área de control del punto.
//                canvas.drawCircle(pointToDraw.x, pointToDraw.y, mPointRadius, mPointPaint);
//                // Dibujar borde del área de control del punto.
//                canvas.drawCircle(pointToDraw.x, pointToDraw.y, mPointRadius, mPointBorderPaint);
//                // Dibujar punto central del área de control del punto.
//                canvas.drawCircle(pointToDraw.x, pointToDraw.y, CENTER_POINT_RADIUS, mCenterPointPaint);
//            }
//        }
        super.onDraw(canvas);
    }

    @Override
    protected void computeShape() {
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
    public boolean checkTouchToSelect(PointF point) {
        return mMappedCenter != null &&
            MathUtils.valueWithinRange(
                MathUtils.distanceBetweenPoints(mMappedCenter.x, mMappedCenter.y, point.x, point.y),
                mMappedRadius - TOUCH_RADIUS,
                mMappedRadius + TOUCH_RADIUS);
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
        for (IOnCircleCenterChangeListener listener: mListeners) {
            listener.updateCircleCenterAndRadius(mCenter, mRadius);
        }
    }

    /**
     * Establece la nueva lista de puntos que representan a la circunferencia.
     * @param pointsArray Lista de puntos actualizada.
     */
    public void setPathPoints(float[] pointsArray) {
        mPathPoints = pointsArray;
    }

    //region Tratamiento de listeners.
    @Override
    public void addOnCircleCenterChangeListener(IOnCircleCenterChangeListener listener) {
        mListeners.add(listener);
        listener.updateCircleCenterAndRadius(mCenter, mRadius);
    }

    @Override
    public void removeOnCircleCenterChangeListener(IOnCircleCenterChangeListener listener) {
        try {
            mListeners.remove(listener);
        }
        catch (Exception e) {
            Log.d(TAG, "removeOnMatrixViewChangeListener: the object was not found in the list.");
        }
    }
    //endregion
}