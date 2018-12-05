package barsotti.alejandro.prototipotf.customViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import barsotti.alejandro.prototipotf.Utils.MathUtils;

public class Tangent extends Shape implements IOnCircleCenterChangeListener {
    // Tag utilizado con fines de debug.
    private static final String TAG = "Tangent";
    // Número que indica la cantidad de puntos que componen la tangente.
    private static final int NUMBER_OF_POINTS = 1;
    // Punto que indica las coordenadas del centro de la circunferencia de la que depende la tangente.
    private PointF mCircleCenter;
    private float mCircleRadius;
    // Punto que indica las coordenadas del centro de la circunferencia de la que depende la tangente
    // mapeado según la matriz actual.
    private PointF mMappedCircleCenter;
    // Lista de puntos utilizados para representar las líneas que componen la tangente (tangente + línea
    // radial)
    private float[] mTangentPoints = new float[4];
    // Lista de puntos utilizados para representar las líneas que componen la tangente (tangente + línea
    // radial) mapeados según la matriz actual.
    private float[] mMappedTangentPoints = new float[4];

    //region Constructores
    public Tangent(Context context) {
        this(context, null);
    }

    public Tangent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initializeShape();
        Log.d(TAG, "Constructor.");
    }
    //endregion

    @Override
    protected void onDraw(Canvas canvas) {
        // Dibujar si la figura se encuentra completa.
        if (mMappedTangentPoints != null) {
            // Dibujar recta radial.
            for (PointF pointToDraw: mMappedShapePoints) {
                // Borde
                canvas.drawLine(mMappedCircleCenter.x, mMappedCircleCenter.y, pointToDraw.x, pointToDraw.y,
                    mIsSelected ? mSelectedShapeBorderPaint : mShapeBorderPaint);
                // Línea principal.
                canvas.drawLine(mMappedCircleCenter.x, mMappedCircleCenter.y, pointToDraw.x, pointToDraw.y,
                    mIsSelected ? mSelectedShapePaint : mShapePaint);
            }

            // Dibujar tangente.
            // Borde.
            canvas.drawLines(mMappedTangentPoints, mIsSelected ?
                mSelectedShapeBorderPaint : mShapeBorderPaint);
            // Línea principal.
            canvas.drawLines(mMappedTangentPoints, mIsSelected ? mSelectedShapePaint : mShapePaint);
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
    protected void initializeShape() {
    }

    @Override
    protected int getNumberOfPointsInShape() {
        return NUMBER_OF_POINTS;
    }

    @Override
    protected int getShapeColor() {
        return Color.CYAN;
    }

    @Override
    public boolean checkTouchToSelect(PointF point) {
        // Verificar que el toque se encuentre en un área cercana a algunas de las dos rectas.
        return mMappedCircleCenter != null && // Centro de circunferencia no nulo.

            mMappedShapePoints.size() == NUMBER_OF_POINTS && // Figura completa.

            MathUtils.distanceBetweenLineAndPoint(point,
                    new PointF(mMappedTangentPoints[0], mMappedTangentPoints[1]),
                    new PointF(mMappedTangentPoints[2], mMappedTangentPoints[3])
                ) < TOUCH_RADIUS // Distancia de punto hasta recta tangente.
            ;
    }

    @Override
    protected void computeShape() {
        if (mShapePoints.size() == NUMBER_OF_POINTS) {
            PointF pointInCircumference = MathUtils.translatePointToCircumference(mCircleCenter,
                mCircleRadius, mShapePoints.get(0));
            mShapePoints.set(0, pointInCircumference);
            mTangentPoints = MathUtils.tangentToCircumference(mCircleCenter, pointInCircumference, mCircleRadius);
        }
    }

    @Override
    public void updateViewMatrix(Matrix matrix) {
        super.updateViewMatrix(matrix);

        mMappedCircleCenter = mapPoint(mCurrentMatrix, mCircleCenter);
        mCurrentMatrix.mapPoints(mMappedTangentPoints, mTangentPoints);
        mMappedShapePoints = mapPoints(mCurrentMatrix, mShapePoints);

        invalidate();
    }

    @Override
    public void updateCircleCenterAndRadius(PointF center, float radius) {
        mCircleCenter = new PointF(center.x, center.y);
        mCircleRadius = radius;
        computeShape();
    }
}