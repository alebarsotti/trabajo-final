package barsotti.alejandro.prototipotf.customViews;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import barsotti.alejandro.prototipotf.interfaces.IShape;

public class Circumference extends Drawable implements IShape {
    private PointF center;
    private float radius;
    private PointF point1;
    private PointF point2;
    private PointF point3;

    private Paint paint;

    private PointF Center;
    private float Radius;
    private PointF Point1;
    private PointF Point2;
    private PointF Point3;

    Circumference(PointF point1, PointF point2, PointF point3) {
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.point1 = point1;
        this.point2 = point2;
        this.point3 = point3;
        computeCenterAndRadiusFromPoints();
    }

    public Circumference(PointF center, float radius) {
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.center = center;
        this.radius = radius;
    }

    private PointF mapPoint(Matrix matrix, PointF point) {
        if (point == null) {
            return null;
        }

        float[] floats = { point.x, point.y };
        matrix.mapPoints(floats);

        return new PointF(floats[0], floats[1]);
    }

    public void updateShape(Matrix matrix) {
        Radius = matrix.mapRadius(radius);

        Center = mapPoint(matrix, center);
        Point1 = mapPoint(matrix, point1);
        Point2 = mapPoint(matrix, point2);
        Point3 = mapPoint(matrix, point3);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        if (Center != null && Radius != 0) {
            canvas.drawCircle(Center.x, Center.y, Radius, paint);
        }
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (Point1 != null) {
            canvas.drawPoint(Point1.x, Point1.y, paint);
        }
        if (Point2 != null) {
            canvas.drawPoint(Point2.x, Point2.y, paint);
        }
        if (Point3 != null) {
            canvas.drawPoint(Point3.x, Point3.y, paint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter filter) {
        paint.setColorFilter(filter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    private void computeCenterAndRadiusFromPoints() {
        double a13, b13, c13;
        double a23, b23, c23;
        double x, y, rad;

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
}
