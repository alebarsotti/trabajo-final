package barsotti.alejandro.prototipotf.customViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import barsotti.alejandro.prototipotf.Utils.MathUtils;

public class Circle extends Shape {
    private static final int NUMBER_OF_POINTS = 3;
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
    private Path mPath = new Path();
    private final Path mPathToDraw = new Path();
    private AsyncCircumferenceComputer mAsyncCircumferenceComputer;
    private PointF Center;
    private float Radius;

    private float[] mPointsInCircle;
    private float[] mPointsInCircleDraw;

    public Circle(Context context) {
        this(context, null);
    }

    public Circle(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initializeShape();
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

    private PointF mapPoint(Matrix matrix, PointF point) {
        if (point == null) {
            return null;
        }

        float[] floats = { point.x, point.y };
        matrix.mapPoints(floats);

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

    @Override
    protected void onDraw(Canvas canvas) {

        long s = System.currentTimeMillis();

//        canvas.save();
//        canvas.setMatrix(mCurrentMatrix);
        if (mPointsInCircle != null) {
//            PointF lastPoint = null;
//            for (PointF point : mPointsInCircle) {
//                if (lastPoint != null) {
//                    canvas.drawLine(lastPoint.x, lastPoint.y, point.x, point.y, selectedShapePaint);
//
//                }
//                lastPoint = new PointF(point.x, point.y);
//            }

//            float[] floats = new float[mPointsInCircle.length * 4];
//            int count = 0;
//            for (PointF point: mPointsInCircle) {
//                floats[count++] = point.x;
//                floats[count++] = point.y;
//                if (count != 2) {
//                    floats[count++] = point.x;
//                    floats[count++] = point.y;
//                }
//            }
//            floats[count++] = mPointsInCircle[0].x;
//            floats[count] = mPointsInCircle[0].y;
            selectedShapeBorderPaint.setStrokeJoin(Paint.Join.ROUND);
            shapeBorderPaint.setStrokeJoin(Paint.Join.ROUND);
            selectedShapePaint.setStrokeJoin(Paint.Join.ROUND);
            shapePaint.setStrokeJoin(Paint.Join.ROUND);
            canvas.drawLines(mPointsInCircleDraw, mIsSelected ? selectedShapeBorderPaint : shapeBorderPaint);
            canvas.drawLines(mPointsInCircleDraw, mIsSelected ? selectedShapePaint : shapePaint);
        }
//        canvas.restore();


//        synchronized (mPathToDraw) {
//            canvas.drawPath(mPathToDraw, mIsSelected ? selectedShapeBorderPaint : shapeBorderPaint);
//            canvas.drawPath(mPathToDraw, mIsSelected ? selectedShapePaint : shapePaint);
//        }



//        if (center != null) {
//            canvas.save();
//            canvas.setMatrix(mCurrentMatrix);
////            canvas.drawCircle(center.x, center.y, radius, selectedShapePaint);
//            RectF rectF = new RectF(center.x - radius, center.y - radius,
//                center.x + radius, center.y + radius);
////            canvas.drawOval(rectF, selectedShapePaint);
//            canvas.drawArc(rectF, 45, 315, false, selectedShapePaint);
//            canvas.restore();
//        }

////        synchronized (mPathToDraw) {
////            canvas.drawPath(mPathToDraw, mIsSelected ? selectedShapeBorderPaint : shapeBorderPaint);
////            canvas.drawPath(mPathToDraw, mIsSelected ? selectedShapePaint : shapePaint);
//
//        canvas.save();
//        canvas.setMatrix(mCurrentMatrix);
////            canvas.drawPath(mPathToDraw, shapePaint);
//
//        Path path = new Path(mPath);
//        float[] floats = new float[9];
//        mCurrentMatrix.getValues(floats);
//        Region region = new Region(0, 0, 2000, 2000);
//        path.op(region.getBoundaryPath(), Path.Op.INTERSECT);
//
//
//        canvas.drawPath(path, shapePaint);
//        canvas.restore();
////        if (Center != null) {
////            canvas.drawCircle(Center.x, Center.y, Radius, shapePaint);
////        }
////        }

        // Dibujar puntos solo si la figura está seleccionada.
        if (mIsSelected) {
            for (PointF pointToDraw: mMappedPoints) {
                canvas.drawCircle(pointToDraw.x, pointToDraw.y, (float) mPointRadius, pointPaint);
                canvas.drawCircle(pointToDraw.x, pointToDraw.y, (float) mPointRadius, pointBorderPaint);
                canvas.drawCircle(pointToDraw.x, pointToDraw.y, (float) 2, pointCenterPaint);
            }
        }
        long e = System.currentTimeMillis();
        Log.d(TAG, "onDraw: " + (e-s));
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
        long s = System.currentTimeMillis();
        if (matrix != null) {
            mPreviousMatrix.set(mCurrentMatrix);
            mCurrentMatrix.set(matrix);

            if (mPreviousMatrix == null) {
                mPath.transform(mCurrentMatrix, mPathToDraw);
            }
            else {
                Matrix inverse = new Matrix(mPreviousMatrix);
                inverse.invert(inverse);
                Path path = new Path();
                mPathToDraw.transform(inverse, path);
                path.transform(mCurrentMatrix);
                mPathToDraw.set(path);
            }
        }
        Radius = mCurrentMatrix.mapRadius(radius);
        Center = mapPoint(mCurrentMatrix, center);
        mMappedPoints = mapPoints(mCurrentMatrix, mPoints);

        float[] floats = new float[9];
        mCurrentMatrix.getValues(floats);
        mCurrentZoom = floats[Matrix.MSCALE_X];
//        mInitialZoom = Math.min(mInitialZoom, mCurrentZoom);
        if (mInitialZoom == 0) {
            mInitialZoom = mCurrentZoom;
            mPointRadiusLimit = Math.max(this.getMeasuredWidth(), this.getMeasuredHeight()) / 6;
        }
        mPointRadius = (float) Math.min((POINT_RADIUS * mCurrentZoom / mInitialZoom/* * 1.5d*/), mPointRadiusLimit);

        if (mPointsInCircle != null) {
            if (mPointsInCircleDraw == null) {
                mPointsInCircleDraw = new float[mPointsInCircle.length];
            }
            mCurrentMatrix.mapPoints(mPointsInCircleDraw, mPointsInCircle);
        }

        computeNewCircumferencePath();

        invalidate();
        long e = System.currentTimeMillis();
        Log.d(TAG, "updateViewMatrix: " + (e-s));
    }

    @Override
    public void selectShape(boolean isSelected) {
        super.selectShape(isSelected);
        computeNewCircumferencePath();
    }

    private void computeNewCircumferencePath() {
//        if (Center != null && Radius != 0) {
//            if (mAsyncCircumferenceComputer != null) {
//                mAsyncCircumferenceComputer.cancel(true);
//            }
//
//            mAsyncCircumferenceComputer = new AsyncCircumferenceComputer(this);
//            mAsyncCircumferenceComputer.execute();
//        }
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
//        if (Center != null) {
//            mDrawPath.reset();
//            mDrawPath.addCircle(Center.x, Center.y, Radius, Path.Direction.CW);
//        }
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

    public void setPath(Path path) {
        mPath.set(path);
    }

    public void setPoints(float[] pointsArray) {
        mPointsInCircle = pointsArray;
    }

    public static class AsyncCircumferenceComputer extends AsyncTask<Void, Void, Void> {
        private WeakReference<Circle> circleWeakReference;

        AsyncCircumferenceComputer(Circle circle) {
            this.circleWeakReference = new WeakReference<>(circle);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Circle view = circleWeakReference.get();
            view.invalidate();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Circle view = circleWeakReference.get();

            Path path = new Path();
//            view.mPath.transform(view.mCurrentMatrix, view.mPathToDraw);
            view.mPath.transform(view.mCurrentMatrix, path);
            if (isCancelled()) {
                return null;
            }
//            int delta = 10;
            int deltaX = view.getMeasuredWidth();
            int deltaY = (int) (0.25 * view.getMeasuredHeight());
//            Region region = new Region(-delta, -delta, view.getMeasuredWidth() + 2 * delta, view.getMeasuredHeight() + 2 * delta);
            Region region = new Region(-deltaX, -deltaY, view.getMeasuredWidth() + deltaX, view.getMeasuredHeight() + deltaY);
            if (isCancelled()) {
                return null;
            }

//            view.mPathToDraw.op(region.getBoundaryPath(), Path.Op.INTERSECT);
            path.op(region.getBoundaryPath(), Path.Op.INTERSECT);

//            synchronized (view.mPathToDraw) {
                view.mPathToDraw.set(path);
//            }

            return null;
        }
    }
}
