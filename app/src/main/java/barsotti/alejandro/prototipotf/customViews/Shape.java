package barsotti.alejandro.prototipotf.customViews;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import barsotti.alejandro.prototipotf.Utils.MathUtils;
import barsotti.alejandro.prototipotf.Utils.ViewUtils;

public abstract class Shape extends View implements IOnMatrixViewChangeListener {
    // Mínimo factor de escala permitido para la matriz de la View a la que pertenece la figura.
    protected static final int MIN_SCALE_FACTOR = ViewUtils.MIN_SCALE_FACTOR;
    // Máximo factor de escala permitido para la matriz de la View a la que pertenece la figura.
    protected static final int MAX_SCALE_FACTOR = ViewUtils.MAX_SCALE_FACTOR;
    protected static final float TOUCH_RADIUS = 75;
    protected static final float POINT_RADIUS = 30;
    protected float mPointRadius = POINT_RADIUS;
    private static final String TAG = "Shape";
    protected Integer mSelectedPointIndex;
    protected boolean mIsSelected = false;
    protected ArrayList<PointF> mPoints = new ArrayList<>();
    private GestureDetector mGestureDetector = new GestureDetector(getContext(), new ShapeGestureListener());
    protected ArrayList<PointF> mMappedPoints = new ArrayList<>();
    protected Matrix mCurrentMatrix = new Matrix();
    protected float mCurrentZoom = 0;
    protected float mInitialZoom = 0;
    protected float mOriginalZoom = 0;
    protected float mPointRadiusMaxLimit = 0;
    protected float mPointRadiusMinLimit = 0;

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

    protected void initializePointRadiusRange() {
        mPointRadiusMaxLimit = Math.max(this.getMeasuredWidth(), this.getMeasuredHeight()) / 6;
        mPointRadiusMinLimit = mPointRadiusMaxLimit / 3;
    }

    public void addShapeCreatorListener(IShapeCreator shapeCreator) {
        mOriginalZoom = shapeCreator.getOriginalZoom();
        shapeCreator.addOnMatrixViewChangeListener(this);
    }

    public void selectShape(boolean isSelected) {
        mIsSelected = isSelected;
        invalidate();
    }

    public boolean verifyShapeTouched(PointF point) {
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

        boolean gestureDetectorResponse = mGestureDetector.onTouchEvent(event);
        // Detectar si finalizó un scroll de un punto. De ser así, se debe calcular nuevamente la forma.
        if (event.getAction() == MotionEvent.ACTION_UP) {
            computeShape();
            updateViewMatrix(null);
        }

        return gestureDetectorResponse;
    }

    private class ShapeGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            // Verificar que el toque haya sido cerca de uno de los puntos de la figura. De no ser así, no
            // capturar el evento.
            float eX = e.getX();
            float eY = e.getY();
            for (int i = 0; i < mMappedPoints.size(); i++) {
                PointF point = mMappedPoints.get(i);
                if (MathUtils.distanceBetweenPoints(eX, eY, point.x, point.y) <= mPointRadius) {
                    mSelectedPointIndex = i;
                    return true;
                }
            }

            mSelectedPointIndex = null;

            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Si un punto está seleccionado, desplazarlo según el scroll detectado.
            if (mSelectedPointIndex != null) {
                mPoints.get(mSelectedPointIndex).offset(-distanceX / mCurrentZoom,
                    -distanceY / mCurrentZoom);
                updateViewMatrix(null);

                return true;
            }

            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // Verificar si la figura fue tocada. De no ser así, deseleccionarla.
            verifyShapeTouched(new PointF(e.getX(), e.getY()));

            return true;
        }
    }
}
