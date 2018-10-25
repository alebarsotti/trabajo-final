package barsotti.alejandro.prototipotf.customViews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import barsotti.alejandro.prototipotf.Utils.MathUtils;

public class Circle extends Shape {
    private static final int NUMBER_OF_POINTS = 3;
    private static final String TAG = "Circle";
    private PointF center;
    private float radius;
//    private ArrayList<PointF> mPoints = new ArrayList<>();

    // TODO: Al detectar un toque, se puede crear un path con la forma y un path con el toque, para
    // determinar si se intersecan.

//    private Paint shapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//    private Paint selectedShapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//    private Paint shapeBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//    private Paint selectedShapeBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//    private Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//    private Paint pointBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//    private Paint pointCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint shapePaint = new Paint();
    private Paint selectedShapePaint = new Paint();
    private Paint shapeBorderPaint = new Paint();
    private Paint selectedShapeBorderPaint = new Paint();
    private Paint pointPaint = new Paint();
    private Paint pointBorderPaint = new Paint();
    private Paint pointCenterPaint = new Paint();

    // FIXME: Prueba
    private Path mPath = new Path();
    private Bitmap mBitmap;
    private Canvas mBitmapCanvas;
    private AsyncCircleDrawer mAsyncCircleDrawer;


    private PointF Center;
    private float Radius;

    private Circle(Context context) {
        this(context, (AttributeSet) null);
    }

    private Circle(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initializeShape();
    }

    @Override
    protected void initializeShape() {
        int mShapeColor = Color.RED;
        shapePaint.setColor(mShapeColor);
        shapePaint.setStyle(Paint.Style.STROKE);
        shapePaint.setStrokeWidth(8);

        selectedShapePaint.set(shapePaint);
        selectedShapePaint.setAlpha(127);

        shapeBorderPaint.setColor(Color.BLACK);
        shapeBorderPaint.setStrokeWidth(16);
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
//        pointBorderPaint.setPathEffect(new DashPathEffect(new float[] {4, 4}, 0));
    }

    public Circle(Context context, IShapeCreator shapeCreator) {
        this(context);

        shapeCreator.addOnMatrixViewChangeListener(this);
    }
//    public Circle(Context context, PointF point1, PointF point2, PointF point3) {
//        this(context);
//
//        mPoints.add(point1);
//        mPoints.add(point2);
//        mPoints.add(point3);
//
//        computeCenterAndRadiusFromPoints(point1, point2, point3);
//    }
//
//    public Circle(Context context, PointF center, float radius) {
//        this(context);
//
//        this.shapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        this.center = center;
//        this.radius = radius;
//    }

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
//        if (Center != null && Radius != 0) {
//            canvas.drawCircle(Center.x, Center.y, Radius,
//                mIsSelected ? selectedShapeBorderPaint : shapeBorderPaint);
//            canvas.drawCircle(Center.x, Center.y, Radius,
//                mIsSelected ? selectedShapePaint : shapePaint);


//            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

//            if (mBitmap != null) {
//                canvas.drawBitmap(mBitmap, 0, 0, null);
//            }

        long s = System.currentTimeMillis();
        if (Center != null && Radius != 0 && mPath != null) {
            canvas.drawPath(mPath, mIsSelected ? selectedShapeBorderPaint : shapeBorderPaint);
            canvas.drawPath(mPath, mIsSelected ? selectedShapePaint : shapePaint);
        }
        long e = System.currentTimeMillis();
        if ((e - s) > 5) {
            Log.d(TAG, "onDraw: " + (e - s));
        }





















//        }

        // Dibujar puntos solo si la figura está seleccionada.
        PointF lastPoint = new PointF();

        if (mIsSelected) {
            for (PointF pointToDraw: mMappedPoints) {
                canvas.drawCircle(pointToDraw.x, pointToDraw.y, (float) mPointRadius, pointPaint);
                canvas.drawCircle(pointToDraw.x, pointToDraw.y, (float) mPointRadius, pointBorderPaint);
                canvas.drawCircle(pointToDraw.x, pointToDraw.y, (float) mCurrentZoom, pointCenterPaint);

                if (!lastPoint.equals(0, 0) && !lastPoint.equals(pointToDraw)) {
                    canvas.drawLine(lastPoint.x, lastPoint.y, pointToDraw.x, pointToDraw.y, shapePaint);
                }
                lastPoint.set(pointToDraw);
            }
        }
    }

    @Override
    protected void computeShape() {
        if (mPoints.size() != NUMBER_OF_POINTS) {
            return;
        }

        // FIXME: Prueba de MathUtils.
        MathUtils.circumferenceFromThreePoints(this);

        // TODO: Determinar por qué puse esto acá.
//        mIsSelected = true;
    }

    @Override
    public void updateViewMatrix(Matrix matrix) {
        if (matrix != null) {
            mLastMatrix = matrix;
        }
        Radius = mLastMatrix.mapRadius(radius);
        Center = mapPoint(mLastMatrix, center);
        mMappedPoints = mapPoints(mLastMatrix, mPoints);

        // FIXME: Prueba.
        float[] floats = new float[9];
        mLastMatrix.getValues(floats);
        mCurrentZoom = floats[Matrix.MSCALE_X];
        mPointRadius = (float) (POINT_RADIUS * Math.max(1, mCurrentZoom) * 1.5d);

        drawCircleInBitmap();
//        if (Center != null && Radius != 0) {
//            // FIXME: Prueba de círculo con curvas Bezier.
//            double m = 0.551915024494d;
//            double deltaM = m * Radius;
//
//            mPath.reset();
//            mPath.moveTo(Center.x, Center.y + Radius);
//            mPath.cubicTo((float) (Center.x + deltaM), Center.y + Radius,
//                Center.x + Radius, (float) (Center.y + deltaM),
//                Center.x + Radius, Center.y);
//            mPath.cubicTo(Center.x + Radius, (float) (Center.y - deltaM),
//                (float) (Center.x + deltaM), Center.y - Radius,
//                Center.x, Center.y - Radius);
//            mPath.cubicTo((float) (Center.x - deltaM), Center.y - Radius,
//                Center.x - Radius, (float) (Center.y - deltaM),
//                Center.x - Radius, Center.y);
//            mPath.cubicTo(Center.x - Radius, (float) (Center.y + deltaM),
//                (float) (Center.x - deltaM), Center.y + Radius,
//                Center.x, Center.y + Radius);
//            mPath.close();
////            canvas.drawPath(path, mIsSelected ? selectedShapeBorderPaint : shapeBorderPaint);
////            canvas.drawPath(path, mIsSelected ? selectedShapePaint : shapePaint);
//
//            mBitmap = Bitmap.createBitmap(this.getMeasuredWidth(), this.getMeasuredHeight(),
//                Bitmap.Config.ARGB_8888);
//
//            mBitmapCanvas = new Canvas(mBitmap);
//
//            mBitmapCanvas.save();
////            bitmapCanvas.translate(offsetX, offsetY);
////            bitmapCanvas.drawColor(0xfff9f9f9);
//            mBitmapCanvas.drawPath(mPath, mIsSelected ? selectedShapeBorderPaint : shapeBorderPaint);
//            mBitmapCanvas.drawPath(mPath, mIsSelected ? selectedShapePaint : shapePaint);
//            mBitmapCanvas.restore();
//        }

        invalidate();
    }

    @Override
    public void selectShape(boolean isSelected) {
        super.selectShape(isSelected);
        drawCircleInBitmap();
    }

    private void drawCircleInBitmap() {
        // FIXME: Prueba de círculo con curvas Bezier.
        if (Center != null && Radius != 0) {
            if (mAsyncCircleDrawer != null) {
                mAsyncCircleDrawer.cancel(true);
            }

            mAsyncCircleDrawer = new AsyncCircleDrawer(this);
            mAsyncCircleDrawer.execute();
        }
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

    public static class AsyncCircleDrawer extends AsyncTask<Void, Void, Void> {
        private WeakReference<Circle> circleWeakReference;
        private final double mBezierCurveConstant = 0.551915024494d;

        AsyncCircleDrawer(Circle circle) {
            this.circleWeakReference = new WeakReference<>(circle);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Circle view = circleWeakReference.get();
            view.invalidate();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            long start = System.currentTimeMillis();
            Circle view = circleWeakReference.get();

            // FIXME: Prueba de círculo con curvas Bezier.
//                double m = 0.551915024494d;
            double deltaM = mBezierCurveConstant * view.Radius;

            Path newPath = new Path();
            newPath.reset();
            newPath.moveTo(view.Center.x, view.Center.y + view.Radius);
            newPath.cubicTo((float) (view.Center.x + deltaM), view.Center.y + view.Radius,
                view.Center.x + view.Radius, (float) (view.Center.y + deltaM),
                view.Center.x + view.Radius, view.Center.y);
            newPath.cubicTo(view.Center.x + view.Radius, (float) (view.Center.y - deltaM),
                (float) (view.Center.x + deltaM), view.Center.y - view.Radius,
                view.Center.x, view.Center.y - view.Radius);
            newPath.cubicTo((float) (view.Center.x - deltaM), view.Center.y - view.Radius,
                view.Center.x - view.Radius, (float) (view.Center.y - deltaM),
                view.Center.x - view.Radius, view.Center.y);
            newPath.cubicTo(view.Center.x - view.Radius, (float) (view.Center.y + deltaM),
                (float) (view.Center.x - deltaM), view.Center.y + view.Radius,
                view.Center.x, view.Center.y + view.Radius);
            newPath.close();

            Region region = new Region(-10, -10, view.getMeasuredWidth() + 10, view.getMeasuredHeight() + 10);
            newPath.op(region.getBoundaryPath(), Path.Op.INTERSECT);

            view.mPath.set(newPath);

            return null;
        }
    }
}
