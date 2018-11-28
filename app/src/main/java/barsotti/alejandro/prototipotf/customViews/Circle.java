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
    private static final String TAG = "Circle";
    private static final int NUMBER_OF_POINTS = 3;
    public static final int NUMBER_OF_POINTS_TO_DRAW = 360;
    private PointF mCenter;
    private float mRadius;
    public PointF mMappedCenter;
    public float mMappedRadius;
    private float[] mPathPoints = new float[NUMBER_OF_POINTS_TO_DRAW * 4];
    private float[] mMappedPathPoints = new float[NUMBER_OF_POINTS_TO_DRAW * 4];
    private ArrayList<IOnCircleCenterChangeListener> mListeners = new ArrayList<>();


    public Circle(Context context) {
        this(context, null);
    }

    public Circle(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initializeShape();
        Log.d(TAG, "Constructor.");
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
    protected void computeShape() {
        MathUtils.circumferenceFromThreePoints(this);

        // TODO: Determinar por qué puse esto acá.
//        mIsSelected = true;
    }

    @Override
    public void updateViewMatrix(Matrix matrix) {
//        if (mPointRadiusMaxLimit == 0) {
//            initializePointRadiusRange();
//        }
//        if (matrix != null) {
//            mCurrentMatrix.set(matrix);
//        }
        super.updateViewMatrix(matrix);

        mMappedRadius = mCurrentMatrix.mapRadius(mRadius);
        mMappedCenter = mapPoint(mCurrentMatrix, mCenter);
        mMappedShapePoints = mapPoints(mCurrentMatrix, mShapePoints);

//        float[] floats = new float[9];
//        mCurrentMatrix.getValues(floats);
//        mCurrentZoom = floats[Matrix.MSCALE_X];
//        float realZoom = mCurrentZoom / mOriginalZoom;
//        // Calcular porcentaje del rango [MIN_SCALE_FACTOR, MAX_SCALE_FACTOR] al que equivale realZoom.
//        float percentage = (realZoom - MIN_SCALE_FACTOR) / (MAX_SCALE_FACTOR - MIN_SCALE_FACTOR);
//        mPointRadius = mPointRadiusMinLimit + (mPointRadiusMaxLimit - mPointRadiusMinLimit) * percentage;

        mCurrentMatrix.mapPoints(mMappedPathPoints, mPathPoints);

        invalidate();
    }

//    @Override
//    public boolean addPoint(PointF point) {
//        if (mShapePoints.size() < NUMBER_OF_POINTS) {
//            mShapePoints.add(point);
//            computeShape();
//
//            invalidate();
//        }
//
//        return mShapePoints.size() < NUMBER_OF_POINTS;
//    }

    @Override
    public boolean checkTouchToSelect(PointF point) {
        return mMappedCenter != null &&
            MathUtils.valueWithinRange(
                MathUtils.distanceBetweenPoints(mMappedCenter.x, mMappedCenter.y, point.x, point.y),
                mMappedRadius - TOUCH_RADIUS,
                mMappedRadius + TOUCH_RADIUS);
    }

//    public ArrayList<PointF> getPointArray() {
//        return mShapePoints;
//    }

    public void setCenterAndRadius(PointF newCenter, float newRadius) {
        mCenter = newCenter;
        mRadius = newRadius;

        for (IOnCircleCenterChangeListener listener: mListeners) {
            listener.updateCircleCenterAndRadius(mCenter, mRadius);
        }
    }

    public void setPathPoints(float[] pointsArray) {
        mPathPoints = pointsArray;
    }

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

//    //region Utilities
//    private PointF mapPoint(Matrix matrix, PointF point) {
//        if (point == null) {
//            return null;
//        }
//
//        // Crear Array con el punto, estructura necesaria para utilizar mapPoints.
//        float[] floats = { point.x, point.y };
//
//        // Mapear el punto.
//        matrix.mapPoints(floats);
//
//        // Crear punto con el resultado del mapeo.
//        return new PointF(floats[0], floats[1]);
//    }
//
//    private ArrayList<PointF> mapPoints(Matrix matrix, ArrayList<PointF> pointsToMap) {
//        // Crear Array con puntos, estructura necesaria para utilizar mapPoints.
//        float[] pointsArray = new float[pointsToMap.size() * 2];
//        for (int i = 0; i < pointsToMap.size(); i++) {
//            PointF point = pointsToMap.get(i);
//            pointsArray[i * 2] = point.x;
//            pointsArray[i * 2 + 1] = point.y;
//        }
//
//        // Mapear los puntos.
//        matrix.mapPoints(pointsArray);
//
//        // Crear ArrayList resultado con los puntos mapeados.
//        ArrayList<PointF> mappedPoints = new ArrayList<>();
//        for (int i = 0; i < pointsToMap.size(); i++) {
//            mappedPoints.add(new PointF(pointsArray[i * 2], pointsArray[i * 2 + 1]));
//        }
//
//        return mappedPoints;
//    }
//    //endregion
}