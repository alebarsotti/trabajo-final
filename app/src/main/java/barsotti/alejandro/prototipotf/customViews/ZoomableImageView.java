package barsotti.alejandro.prototipotf.customViews;

import android.content.Context;
import android.gesture.Gesture;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class ZoomableImageView extends android.support.v7.widget.AppCompatImageView {
    private static final float MAX_SCALE_FACTOR = 5;
    private static final float MIN_SCALE_FACTOR = 1;

//    private int mViewWidth;
//    private int mViewHeight;
    private Bitmap mBitmap;
    private Matrix mDefaultMatrix;
    private Matrix mCurrentMatrix;
    private float mCurrentScaleFactor;
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mGestureDetector;
    private float mStartX;
    private float mStartY;
    private float mTranslateX;
    private float mTranslateY;
    private float mLastX;
    private float mLastY;

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
                float scale = (MAX_SCALE_FACTOR + MIN_SCALE_FACTOR) / 3;
                int px = getWidth() / 2;
                int py = getHeight() / 2;
                mCurrentMatrix.postScale(scale, scale, px, py);
            }
//            setMatrix();

            return true;
//            if (mCurrentScaleFactor == MIN_SCALE_FACTOR) {
//                mCurrentScaleFactor = (MAX_SCALE_FACTOR + MIN_SCALE_FACTOR) / 3;
//            }
//            else {
//                mCurrentScaleFactor = MIN_SCALE_FACTOR;
//            }
//            invalidate();
//            requestLayout();
//            return super.onDoubleTap(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // TODO: Evitar scroll si no se hizo zoom aún.
            distanceX = -distanceX;
            distanceY = -distanceY;
            int viewWidth = getWidth();
            int viewHeight = getHeight();

            float[] matrixValues = new float[9];
            mCurrentMatrix.getValues(matrixValues);

            float bitmapWidth = mBitmap.getWidth() * matrixValues[Matrix.MSCALE_X];
            float bitmapHeight = mBitmap.getHeight() * matrixValues[Matrix.MSCALE_Y];
            float translateX = matrixValues[Matrix.MTRANS_X];
            float translateY = matrixValues[Matrix.MTRANS_Y];

            // Avoid panning out of the image (left).
            if (translateX + distanceX > 0) {
                distanceX = -translateX;
            }
            // Avoid panning out of the image (right).
            else if (translateX + distanceX + bitmapWidth < viewWidth) {
                distanceX = viewWidth - translateX - bitmapWidth;
            }
            // Avoid panning out of the image (top).
            if (translateY + distanceY > 0) {
                distanceY = -translateY;
            }
            // Avoid panning out of the image (bottom).
            else if (translateY + distanceY + bitmapHeight < viewHeight) {
                distanceY = viewHeight - translateY - bitmapHeight;
            }



            mCurrentMatrix.postTranslate(distanceX, distanceY);
//            setMatrix();

            return true;
//            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    private class ZoomableImageViewScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // TODO: Evitar que la escala se reduzca por debajo del mínimo (tamaño inicial).
            float scaleFactor = detector.getScaleFactor();
            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();
            mCurrentMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
//            setMatrix();

            return true;


//            mCurrentScaleFactor *= detector.getScaleFactor();
//            mCurrentScaleFactor = Math.min(MAX_SCALE_FACTOR,
//                Math.max(MIN_SCALE_FACTOR, mCurrentScaleFactor));
////            return super.onScale(detector);
//            return true;
        }
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        mTranslateX = 0;
//        mTranslateY = 0;
//        switch (event.getAction() & MotionEvent.ACTION_MASK) {
//            case MotionEvent.ACTION_DOWN:
//                mCurrentState = STATE.PAN;
//                mStartX = event.getX() - mLastX;
//                mStartY = event.getY() - mLastY;
//                break;
//
//            case MotionEvent.ACTION_UP:
//                mCurrentState = STATE.NONE;
//                mLastX = mTranslateX;
//                mLastY = mTranslateY;
//                break;
//
//            case MotionEvent.ACTION_MOVE:
//                if (mCurrentState.equals(STATE.PAN)) {
//                    mTranslateX = event.getX() - mStartX;
//                    mTranslateY = event.getY() - mStartY;
//                }
//                break;
//
//            case MotionEvent.ACTION_POINTER_DOWN:
//                mCurrentState = STATE.ZOOM;
//                break;
//        }

        mScaleGestureDetector.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);
        this.setImageMatrix(mCurrentMatrix);
//        this.setImageMatrix();
//
//        if ((mCurrentState.equals(STATE.PAN) && mCurrentScaleFactor != MIN_SCALE_FACTOR)
//            || mCurrentState.equals(STATE.ZOOM)) {
//            invalidate();
//            requestLayout();
//        }

        return true;
    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//
////        canvas.save();
////        canvas.scale(mCurrentScaleFactor, mCurrentScaleFactor);
//////        canvas.
//////        canvas.translate(mLastX, mLastY);
////        canvas.translate(mTranslateX / mCurrentScaleFactor, mTranslateY / mCurrentScaleFactor);
////        canvas.drawBitmap(mOriginalBitmap, 0, 0, null);
////        canvas.restore();
//    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//
//        int minWidth = MeasureSpec.getSize(widthMeasureSpec);
//        int minHeight = MeasureSpec.getSize(heightMeasureSpec);
//        int scaledViewWidth = (int) (mViewWidth * mCurrentScaleFactor);
//        int scaledViewHeight = (int) (mViewHeight * mCurrentScaleFactor);
//
//        setMeasuredDimension(
//            Math.min(minWidth, scaledViewWidth),
//            Math.min(minHeight, scaledViewHeight)
//        );
//    }

    //region Setters
    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
//        float bitmapAspectRatio = bitmapHeight / (float) bitmapWidth;

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;

        float scale = displayWidth / (float) bitmapWidth;

//        mViewWidth = displayWidth;
//        mViewHeight = Math.round(displayWidth * bitmapAspectRatio);
//        mBitmap = Bitmap.createScaledBitmap(mOriginalBitmap, mViewWidth, mViewHeight, false);
//        invalidate();
//        requestLayout();
        this.setImageBitmap(mBitmap);
        mCurrentMatrix = new Matrix();
        RectF dest = new RectF(0, 0, displayWidth, displayHeight);
        RectF src = new RectF(0, 0, bitmapWidth, bitmapHeight);
        mCurrentMatrix.setRectToRect(src, dest, Matrix.ScaleToFit.CENTER);
        mDefaultMatrix = new Matrix(mCurrentMatrix);
        this.setImageMatrix(mCurrentMatrix);
//        matrix.setScale(scale, scale);
//        matrix.setScale(2, 2);
//        this.setImageMatrix(matrix);
//        matrix.setScale(-2, -2);
//        this.setImageMatrix(matrix);
    }


    private void initializeMembers() {
        this.setScaleType(ScaleType.MATRIX);
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ZoomableImageViewScaleListener());
        mGestureDetector = new GestureDetector(getContext(), new ZoomableImageViewGestureListener());
        mCurrentScaleFactor = MIN_SCALE_FACTOR;
    }
//
//    private void setMatrix() {
//        this.setImageMatrix(mCurrentMatrix);
//    }
    //endregion
}
