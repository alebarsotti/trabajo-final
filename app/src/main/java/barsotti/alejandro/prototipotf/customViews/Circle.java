package barsotti.alejandro.prototipotf.customViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;

import barsotti.alejandro.prototipotf.Utils.MathUtils;

public class Circle extends Shape {
    private static final int NUMBER_OF_POINTS = 3;
    public static final int NUMBER_OF_POINTS_TO_DRAW = 360;
    private static final String TAG = "Circle";
    private PointF center;
    private float radius;
    private Paint shapePaint = new Paint();
    private Paint selectedShapePaint = new Paint();
    private Paint shapeBorderPaint = new Paint();
    private Paint selectedShapeBorderPaint = new Paint();
    private Paint pointPaint = new Paint();
    private Paint pointBorderPaint = new Paint();
    private Paint pointCenterPaint = new Paint();
    private PointF Center;
    private float Radius;

    private float[] mPointsInCircle = new float[NUMBER_OF_POINTS_TO_DRAW * 4];
    private float[] mPointsInCircleDraw = new float[NUMBER_OF_POINTS_TO_DRAW * 4];

    public Circle(Context context) {
        this(context, null);
    }

    public Circle(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initializeShape();
        Log.d(TAG, "Circle: constructor.");
    }

    @Override
    protected void initializeShape() {
        int mShapeColor = Color.RED;
        shapePaint.setColor(mShapeColor);
        shapePaint.setStyle(Paint.Style.STROKE);
        shapePaint.setStrokeWidth(4);

        selectedShapePaint.set(shapePaint);
        selectedShapePaint.setAlpha(127);

        shapeBorderPaint.setColor(Color.BLACK);
        shapeBorderPaint.setStrokeWidth(8);
        shapeBorderPaint.setStyle(Paint.Style.STROKE);

        selectedShapeBorderPaint.set(shapeBorderPaint);
        selectedShapeBorderPaint.setAlpha(127);

        pointPaint.setColor(Color.YELLOW);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setStrokeWidth(20);
        pointPaint.setAlpha(63);

        pointCenterPaint.set(shapeBorderPaint);
        pointCenterPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        pointCenterPaint.setStrokeWidth(2);
        pointCenterPaint.setAlpha(127);

        pointBorderPaint.set(selectedShapeBorderPaint);
        pointBorderPaint.setStrokeWidth(2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Dibujar circunferencia si la misma se encuentra completa.
        if (mPointsInCircle != null) {
//            // Dibujar borde.
//            canvas.drawLines(mPointsInCircleDraw, mIsSelected ? selectedShapeBorderPaint : shapeBorderPaint);
            // Dibujar línea principal.
            canvas.drawLines(mPointsInCircleDraw, mIsSelected ? selectedShapePaint : shapePaint);
        }

        // Dibujar puntos solo si la figura está seleccionada.
        if (mIsSelected) {
            for (PointF pointToDraw: mMappedPoints) {
                // Dibujar relleno del área de control del punto.
                canvas.drawCircle(pointToDraw.x, pointToDraw.y, mPointRadius, pointPaint);
                // Dibujar borde del área de control del punto.
                canvas.drawCircle(pointToDraw.x, pointToDraw.y, mPointRadius, pointBorderPaint);
                // Dibujar punto central del área de control del punto.
                canvas.drawCircle(pointToDraw.x, pointToDraw.y, 2, pointCenterPaint);
            }
        }
    }

    @Override
    protected void computeShape() {
        if (mPoints.size() != NUMBER_OF_POINTS) {
            return;
        }

        MathUtils.circumferenceFromThreePoints(this);

        // TODO: Determinar por qué puse esto acá.
//        mIsSelected = true;
    }

    @Override
    public void updateViewMatrix(Matrix matrix) {
        if (mPointRadiusMaxLimit == 0) {
            initializePointRadiusRange();
        }
        if (matrix != null) {
            mCurrentMatrix.set(matrix);
        }
        Radius = mCurrentMatrix.mapRadius(radius);
        Center = mapPoint(mCurrentMatrix, center);
        mMappedPoints = mapPoints(mCurrentMatrix, mPoints);

        float[] floats = new float[9];
        mCurrentMatrix.getValues(floats);
        mCurrentZoom = floats[Matrix.MSCALE_X];
        float realZoom = mCurrentZoom / mOriginalZoom;
        // Calcular porcentaje del rango [MIN_SCALE_FACTOR, MAX_SCALE_FACTOR] al que equivale realZoom.
        float percentage = (realZoom - MIN_SCALE_FACTOR) / (MAX_SCALE_FACTOR - MIN_SCALE_FACTOR);
        mPointRadius = mPointRadiusMinLimit + (mPointRadiusMaxLimit - mPointRadiusMinLimit) * percentage;

        mCurrentMatrix.mapPoints(mPointsInCircleDraw, mPointsInCircle);

        invalidate();
    }

    @Override
    public void selectShape(boolean isSelected) {
        super.selectShape(isSelected);
    }

    @Override
    public boolean addPoint(PointF point) {
        if (mPoints.size() < NUMBER_OF_POINTS) {
            mPoints.add(point);
            computeShape();

            invalidate();
        }

        return mPoints.size() < NUMBER_OF_POINTS;
    }

    @Override
    public boolean checkTouchToSelect(PointF point) {
        return Center != null &&
            MathUtils.valueWithinRange(
                MathUtils.distanceBetweenPoints(Center.x, Center.y, point.x, point.y),
                Radius - TOUCH_RADIUS,
                Radius + TOUCH_RADIUS);
    }

    public ArrayList<PointF> getPoints() {
        return mPoints;
    }

    public void setCenterAndRadius(PointF newCenter, float newRadius) {
        center = newCenter;
        radius = newRadius;
    }

    public void setPoints(float[] pointsArray) {
        mPointsInCircle = pointsArray;
    }

    //region Utilities
    private PointF mapPoint(Matrix matrix, PointF point) {
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

    private ArrayList<PointF> mapPoints(Matrix matrix, ArrayList<PointF> pointsToMap) {
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
}