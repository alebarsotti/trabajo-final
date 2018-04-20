package barsotti.alejandro.prototipotf.customViews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class ZoomableImageView extends android.support.v7.widget.AppCompatImageView {
    private static final int MAX_ZOOM_SCALE = 15;

    private float mMaxScaleFactor;
    private float mMinScaleFactor;
    private Bitmap mBitmap;
    private Matrix mDefaultMatrix;
    private Matrix mCurrentMatrix;
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mGestureDetector;

    private enum STATE {
        ZOOM,
        PAN,
        NONE
    }
    private STATE mCurrentState = STATE.NONE;

    //region Constructors
    public ZoomableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeMembers();
    }

    public ZoomableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeMembers();
    }

    public ZoomableImageView(Context context) {
        super(context);
        initializeMembers();
    }
    //endregion

    private class ZoomableImageViewGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (!mCurrentMatrix.equals(mDefaultMatrix)) {
                mCurrentMatrix = new Matrix(mDefaultMatrix);
            }
            else {
                float scale = (mMaxScaleFactor + mMinScaleFactor) / 3;
                int px = getWidth() / 2;
                int py = getHeight() / 2;
                mCurrentMatrix.postScale(scale, scale, px, py);
            }

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            distanceX = -distanceX;
            distanceY = -distanceY;
            int viewWidth = getWidth();
            int viewHeight = getHeight();

            float[] matrixValues = new float[9];
            mCurrentMatrix.getValues(matrixValues);
            float translateX = matrixValues[Matrix.MTRANS_X];
            float translateY = matrixValues[Matrix.MTRANS_Y];

            float bitmapWidth = mBitmap.getWidth() * matrixValues[Matrix.MSCALE_X];
            float bitmapHeight = mBitmap.getHeight() * matrixValues[Matrix.MSCALE_Y];

            // Controlar desplazamiento en X.
            float dX = viewWidth - bitmapWidth;
            float minTranslateX = dX < 0 ? dX : 0;
            float maxTranslateX = dX < 0 ? 0 : dX;
            float newTranslateX = translateX + distanceX;

            if (newTranslateX > maxTranslateX) {
                distanceX = maxTranslateX - translateX;
            }
            else if (newTranslateX < minTranslateX) {
                distanceX = minTranslateX - translateX;
            }

            // Controlar desplazamiento en Y.
            float dY = viewHeight - bitmapHeight;
            float minTranslateY = dY < 0 ? dY : 0;
            float maxTranslateY = dY < 0 ? 0 : dY;
            float newTranslateY = translateY + distanceY;

            if (newTranslateY > maxTranslateY) {
                distanceY = maxTranslateY - translateY;
            }
            else if (newTranslateY < minTranslateY) {
                distanceY = minTranslateY - translateY;
            }

            // Desplazar matriz.
            mCurrentMatrix.postTranslate(distanceX, distanceY);

            return true;
        }
    }

    private class ZoomableImageViewScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();

            float[] matrixValues = new float[9];
            mCurrentMatrix.getValues(matrixValues);
            float currentScaleFactor = matrixValues[Matrix.MSCALE_X];

            if (currentScaleFactor * scaleFactor < mMinScaleFactor) {
                mCurrentMatrix = new Matrix(mDefaultMatrix);
            }
            else {
                if (currentScaleFactor * scaleFactor > mMaxScaleFactor) {
                    scaleFactor = mMaxScaleFactor / currentScaleFactor;
                }
                mCurrentMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
            }

            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);
        this.setImageMatrix(mCurrentMatrix);

        return true;
    }

    //region Setters
    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        int bitmapWidth = mBitmap.getWidth();
        int bitmapHeight = mBitmap.getHeight();
        this.setImageBitmap(mBitmap);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;

        mCurrentMatrix = new Matrix();
        RectF dest = new RectF(0, 0, displayWidth, displayHeight);
        RectF src = new RectF(0, 0, bitmapWidth, bitmapHeight);
        mCurrentMatrix.setRectToRect(src, dest, Matrix.ScaleToFit.CENTER);
        mDefaultMatrix = new Matrix(mCurrentMatrix);

        // Set the values for the Min and Max scale factor.
        setZoomValues();

        this.setImageMatrix(mCurrentMatrix);
    }

    private void setZoomValues() {
        float[] matrixValues = new float[9];
        mDefaultMatrix.getValues(matrixValues);

        // El factor de escala mínimo será el que se setea por defecto al escalar la imagen a la vista.
        mMinScaleFactor = matrixValues[Matrix.MSCALE_X];

        // El factor de escala máximo será el resultado de multiplicar el factor de escala mínimo por el
        // factor de escala de zoom máximo configurado.
        mMaxScaleFactor = mMinScaleFactor * MAX_ZOOM_SCALE;
    }


    private void initializeMembers() {
        this.setScaleType(ScaleType.MATRIX);
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ZoomableImageViewScaleListener());
        mGestureDetector = new GestureDetector(getContext(), new ZoomableImageViewGestureListener());
    }
}
