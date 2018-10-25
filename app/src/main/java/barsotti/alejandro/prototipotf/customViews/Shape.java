package barsotti.alejandro.prototipotf.customViews;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import barsotti.alejandro.prototipotf.Utils.MathUtils;

public abstract class Shape extends View implements IOnMatrixViewChangeListener {
    protected static final float TOUCH_RADIUS = 75;
    protected static final double POINT_RADIUS = 20;
    protected double mPointRadius = POINT_RADIUS;
    private static final String TAG = "Shape";
    protected Integer mSelectedPointIndex;
    protected boolean mIsSelected = false;
    protected PointF mTouch;
    protected Path mDrawPath = new Path();
    protected ArrayList<PointF> mPoints = new ArrayList<>();
    private GestureDetector mGestureDetector = new GestureDetector(getContext(), new ShapeGestureListener());
    protected ArrayList<PointF> mMappedPoints = new ArrayList<>();
    protected Matrix mLastMatrix;
    protected double mCurrentZoom;

    // FIXME: Prueba
    protected ArrayList<PointF> mTouchList;

    //region Constructors
    public Shape(Context context) {
        this(context, null);
    }

    public Shape(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    //endregion

    //region Abstract Methods
    protected abstract void initializeShape();

    public abstract boolean addPoint(PointF point);

    public abstract boolean checkTouchToSelect(PointF point);

    protected abstract void computeShape();

    @Override
    public abstract void updateViewMatrix(Matrix matrix);
    //endregion

    public void selectShape(boolean isSelected) {
        mIsSelected = isSelected;
        invalidate();
    }

    public boolean verifyShapeTouched(PointF point) {
//        checkTouchToSelect();
//        Path touch = new Path();
//        touch.addCircle(point.x, point.y, TOUCH_RADIUS, Path.Direction.CW);
//
//        boolean op = touch.op(mDrawPath, Path.Op.INTERSECT);
//
//        selectShape(op && !touch.isEmpty());

        selectShape(checkTouchToSelect(point));

        return mIsSelected;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsSelected) {
            return false;
        }

//        // FIXME: Descomentar.
//        boolean pointTouched = false;
//        for (PointF point: mPoints) {
//            if (Math.sqrt(Math.pow(event.getX() - point.x, 2) + Math.pow(event.getY() - point.y, 2))
//                < TOUCH_RADIUS) {
//                pointTouched = true;
//            }
//        }
//        if (!pointTouched) {
//            return false;
//        }

//        return true;
//        return false;
        boolean gestureDetectorResponse = mGestureDetector.onTouchEvent(event);
        // Detectar si finalizó un scroll de un punto. De ser así, se debe calcular nuevamente la forma.
        if (event.getAction() == MotionEvent.ACTION_UP) {
            computeShape();
            updateViewMatrix(null);
        }

        return gestureDetectorResponse;

//        mGestureDetector.onTouchEvent(event);
//        return super.onTouchEvent(event);
    }

    private class ShapeGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
//            mTouchList = new ArrayList<>();
//            mTouchList.add(new PointF(e.getX(), e.getY()));
//
//            if (mTouch == null) {
//                mTouch = new PointF();
//            }
//            mTouch.set(e.getX(), e.getY());
//            // FIXME: Invalidate utilizado para mostrar el toque, quitar luego.
//            invalidate();
//
//            return false;



//            for (PointF point: mMappedPoints) {
//                if (MathUtils.distanceBetweenPoints(e.getX(), e.getY(), point.x, point.y) <=
//                    TOUCH_RADIUS) {
//                    return true;
//                }
//            }
            for (int i = 0; i < mMappedPoints.size(); i++) {
                PointF point = mMappedPoints.get(i);
                if (MathUtils.distanceBetweenPoints(e.getX(), e.getY(), point.x, point.y) <=
                    mPointRadius * 1.25) {
                    mSelectedPointIndex = i;
                    return true;
                }
            }

            mSelectedPointIndex = null;

            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            mTouchList.add(new PointF(e2.getX(), e2.getY()));
            if (mSelectedPointIndex != null) {
                mPoints.get(mSelectedPointIndex).offset((float) (-distanceX / mCurrentZoom),
                    (float) (-distanceY / mCurrentZoom));
                updateViewMatrix(null);

                return true;
            }

            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            verifyShapeTouched(new PointF(e.getX(), e.getY()));

            return true;
        }
    }
}
