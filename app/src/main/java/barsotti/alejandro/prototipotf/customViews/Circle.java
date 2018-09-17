package barsotti.alejandro.prototipotf.customViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class Circle extends View implements IOnMatrixViewChangeListener, IShape {
    private static final int NUMBER_OF_POINTS = 3;
    private PointF center;
    private float radius;
    private ArrayList<PointF> points = new ArrayList<>();

    private Paint circumferencePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private PointF Center;
    private float Radius;
    private ArrayList<PointF> Points = new ArrayList<>();

    private Circle(Context context) {
        this(context, (AttributeSet) null);
    }

    private Circle(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        circumferencePaint.setColor(Color.RED);
        circumferencePaint.setStyle(Paint.Style.STROKE);
        circumferencePaint.setStrokeWidth(5);
        pointPaint.setColor(Color.GREEN);
        pointPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        pointPaint.setStrokeWidth(10);
    }

    public Circle(Context context, IShapeCreator shapeCreator) {
        this(context);

        shapeCreator.addOnMatrixViewChangeListener(this);
    }
//    public Circle(Context context, PointF point1, PointF point2, PointF point3) {
//        this(context);
//
//        points.add(point1);
//        points.add(point2);
//        points.add(point3);
//
//        computeCenterAndRadiusFromPoints(point1, point2, point3);
//    }
//
//    public Circle(Context context, PointF center, float radius) {
//        this(context);
//
//        this.circumferencePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
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
        if (Center != null && Radius != 0) {
            canvas.drawCircle(Center.x, Center.y, Radius, circumferencePaint);
        }

        for (PointF pointToDraw: Points) {
            canvas.drawPoint(pointToDraw.x, pointToDraw.y, pointPaint);
        }
    }

    private void computeCenterAndRadiusFromPoints() {
        if (points.size() != NUMBER_OF_POINTS) {
            return;
        }

        double a13, b13, c13;
        double a23, b23, c23;
        double x, y, rad;
        PointF point1, point2, point3;
        point1 = points.get(0);
        point2 = points.get(1);
        point3 = points.get(2);

        // begin pre-calculations for linear system reduction
        a13 = 2 * (point1.x - point3.x);
        b13 = 2 * (point1.y - point3.y);
        c13 = (point1.y * point1.y - point3.y * point3.y) + (point1.x * point1.x - point3.x * point3.x);
        a23 = 2 * (point2.x - point3.x);
        b23 = 2 * (point2.y - point3.y);
        c23 = (point2.y * point2.y - point3.y * point3.y) + (point2.x * point2.x - point3.x * point3.x);
        // testsuite-suite to be certain we have three distinct points passed
        double smallNumber = 0.01;
        if ((Math.abs(a13) < smallNumber && Math.abs(b13) < smallNumber)
            || (Math.abs(a13) < smallNumber && Math.abs(b13) < smallNumber)) {
            // // points too close so set to default circle
            x = 0;
            y = 0;
            rad = 0;
        } else {
            // everything is acceptable do the y calculation
            y = (a13 * c23 - a23 * c13) / (a13 * b23 - a23 * b13);
            // x calculation
            // choose best formula for calculation
            if (Math.abs(a13) > Math.abs(a23)) {
                x = (c13 - b13 * y) / a13;
            } else {
                x = (c23 - b23 * y) / a23;
            }
            // radius calculation
            rad = Math.sqrt((x - point1.x) * (x - point1.x) + (y - point1.y) * (y - point1.y));
        }

        radius = (float) rad;
        center = new PointF((float) x, (float) y);
    }

    @Override
    public void updateViewMatrix(Matrix matrix) {
        Radius = matrix.mapRadius(radius);
        Center = mapPoint(matrix, center);
        Points = mapPoints(matrix, points);

        invalidate();
    }

    @Override
    public boolean addPoint(PointF point) {
        if (points.size() < NUMBER_OF_POINTS) {
            points.add(point);
            computeCenterAndRadiusFromPoints();

            invalidate();

            return points.size() < NUMBER_OF_POINTS;
        }

        return false;
    }
}
