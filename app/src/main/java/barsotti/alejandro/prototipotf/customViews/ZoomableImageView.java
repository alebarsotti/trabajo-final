package barsotti.alejandro.prototipotf.customViews;

import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.Toast;

public class ZoomableImageView extends android.support.v7.widget.AppCompatImageView {
    private static final int MAX_ZOOM_SCALE = 15;

    private float mMaxScaleFactor;
    private float mMinScaleFactor;
    private int mBitmapWidth;
    private int mBitmapHeight;
    private Matrix mDefaultMatrix;
    private Matrix mCurrentMatrix;
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mGestureDetector;

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Informar del evento a los detectores de gestos y escala.
        mScaleGestureDetector.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);
        this.setImageMatrix(mCurrentMatrix);

        return true;
    }

    //region Setters
    public void setScale(int bitmapWidth, int bitmapHeight) {

        Drawable drawable = this.getDrawable();
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();

        // Establecer variables de tamaño de la imagen.
//        mBitmapWidth = bitmapWidth;
//        mBitmapHeight = bitmapHeight;
        mBitmapWidth = intrinsicWidth;
        mBitmapHeight = intrinsicHeight;

        // Establecer variables de tamaño de la pantalla.
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;

        // Escalar y centrar matriz de la view que contiene la imagen.
        mCurrentMatrix = new Matrix();
        RectF dest = new RectF(0, 0, displayWidth, displayHeight);
        RectF src = new RectF(0, 0, mBitmapWidth, mBitmapHeight);
        mCurrentMatrix.setRectToRect(src, dest, Matrix.ScaleToFit.CENTER);
        mDefaultMatrix = new Matrix(mCurrentMatrix);

        // Establecer los valores para el factor de escala mínimo y máximo.
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
        // Establecer tipo de escala de la vista a Matriz.
        this.setScaleType(ScaleType.MATRIX);

        // Inicializar detectores de gestos y escala.
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ZoomableImageViewScaleListener());
        mGestureDetector = new GestureDetector(getContext(), new ZoomableImageViewGestureListener());
    }
    //endregion

    private class ZoomableImageViewGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (!mCurrentMatrix.equals(mDefaultMatrix)) {
                mCurrentMatrix = new Matrix(mDefaultMatrix);
            }
            else {
                float scale = mMinScaleFactor + (mMaxScaleFactor - mMinScaleFactor) / 4;
                int px = Math.round(mBitmapWidth * scale / 2);
                int py = Math.round(mBitmapHeight * scale / 2);
                mCurrentMatrix.setScale(scale, scale, px, py);
            }

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (Math.abs(distanceX) < 10 && Math.abs(distanceY) < 10) {
                return false;
            }

            distanceX = -distanceX;
            distanceY = -distanceY;

            float[] matrixValues = new float[9];
            mCurrentMatrix.getValues(matrixValues);
            float translateX = matrixValues[Matrix.MTRANS_X];
            float translateY = matrixValues[Matrix.MTRANS_Y];

            float bitmapWidth = mBitmapWidth * matrixValues[Matrix.MSCALE_X];
            float bitmapHeight = mBitmapHeight * matrixValues[Matrix.MSCALE_Y];

            // Controlar desplazamiento en X.
            float dX = getWidth() - bitmapWidth;
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
            float dY = getHeight() - bitmapHeight;
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

            // Desestimar movimiento si el factor de escala es demasiado pequeño.
            if (scaleFactor < 0.1) {
                return false;
            }

            // Obtener factor de escala actual.
            float[] matrixValues = new float[9];
            mCurrentMatrix.getValues(matrixValues);
//            float currentScaleFactor = matrixValues[Matrix.MSCALE_X];

//            // Forzar que el factor de escala se mantenga dentro de los límites establecidos.
//            if (currentScaleFactor * scaleFactor < mMinScaleFactor) {
//                scaleFactor = mMinScaleFactor / currentScaleFactor;
//            }
//            else if (currentScaleFactor * scaleFactor > mMaxScaleFactor) {
//                scaleFactor = mMaxScaleFactor / currentScaleFactor;
//            }

            // Escalar matriz.
            mCurrentMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());

            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            // TODO: Verificar que no se vean zonas negras fuera de la imagen. En tal caso, volver a centrarla.

//            Toast.makeText(getContext(), "Fin del scaling", Toast.LENGTH_SHORT).show();

            // Prueba: Volver imagen a tamaño original.
            final float finalTransX, finalTransY, finalScaleFactor;
            final Interpolator interpolator = new AccelerateDecelerateInterpolator();
            final long startTime = System.currentTimeMillis();
            final long animationDuration = 500;

            // Valores actuales de la matriz (valores iniciales de la animación).
            float[] initialMatrixValues = new float[9];
            mCurrentMatrix.getValues(initialMatrixValues);
            final float initialScaleFactor = initialMatrixValues[Matrix.MSCALE_X];
            final float initialTransX = initialMatrixValues[Matrix.MTRANS_X];
            final float initialTransY = initialMatrixValues[Matrix.MTRANS_Y];

            // Valores por defecto de la matriz (valores finales de la animación de agrandar).
            float[] defaultMatrixValues = new float[9];
            mDefaultMatrix.getValues(defaultMatrixValues);
            finalScaleFactor = mMinScaleFactor;
            finalTransX = defaultMatrixValues[Matrix.MTRANS_X];
            finalTransY = defaultMatrixValues[Matrix.MTRANS_Y];

            if (initialScaleFactor > mMinScaleFactor) {
                return;
            }

//            // Animar - Versión ineficiente
//            ZoomableImageView.this.post(new Runnable() {
//                @Override
//                public void run() {
//                    // Calcular T: progreso (%) de la animación.
//                    float t = (float) (System.currentTimeMillis() - startTime) / animationDuration;
//                    t = t > 1.0f ? 1.0f : t;
//
//                    // Calcular punto actual de la animación.
//                    float currentPointInAnimation = interpolator.getInterpolation(t);
//
//                    float animationScaleFactor = initialScaleFactor +
//                        (finalScaleFactor - initialScaleFactor) * currentPointInAnimation;
//
//                    float animationTransX = initialTransX +
//                        (finalTransX - initialTransX) * currentPointInAnimation;
//
//                    float animationTransY = initialTransY +
//                        (finalTransY - initialTransY) * currentPointInAnimation;
//
//                    mCurrentMatrix.setScale(animationScaleFactor, animationScaleFactor);
//                    mCurrentMatrix.postTranslate(animationTransX, animationTransY);
//                    ZoomableImageView.this.setImageMatrix(mCurrentMatrix);
//
//                    if (t < 1.0f) {
//                        ZoomableImageView.this.post(this);
//                    }
//                }
//            });

//            // Animar - ValueAnimator
            mCurrentMatrix.reset();
            AnimatorSet animatorSet = new AnimatorSet();
            ValueAnimator scaleFactorValueAnimation =
                ValueAnimator.ofFloat(initialScaleFactor, finalScaleFactor);
//            ValueAnimator transXValueAnimator = ValueAnimator.ofFloat(initialTransX, finalTransX);
//            ValueAnimator transYValueAnimator = ValueAnimator.ofFloat(initialTransY, finalTransY);
            scaleFactorValueAnimation.setInterpolator(interpolator);
//            transXValueAnimator.setInterpolator(interpolator);
//            transYValueAnimator.setInterpolator(interpolator);
            scaleFactorValueAnimation.setDuration(animationDuration);
//            transXValueAnimator.setDuration(animationDuration);
//            transYValueAnimator.setDuration(animationDuration);
            scaleFactorValueAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                    float[] matrixValues = new float[9];
//                    mCurrentMatrix.getValues(matrixValues);
//                    float currentScaleFactor = matrixValues[Matrix.MSCALE_X];
//
//                    float newScale = (float) valueAnimator.getAnimatedValue();
//                    mCurrentMatrix
//                        .postScale(newScale / currentScaleFactor, newScale / currentScaleFactor);
//                    ZoomableImageView.this.setImageMatrix(mCurrentMatrix);

                    float animatedFraction = valueAnimator.getAnimatedFraction();
                    float animationScaleFactor = initialScaleFactor +
                        (finalScaleFactor - initialScaleFactor) * animatedFraction;

                    float animationTransX = initialTransX +
                        (finalTransX - initialTransX) * animatedFraction;

                    float animationTransY = initialTransY +
                        (finalTransY - initialTransY) * animatedFraction;
                    mCurrentMatrix.setScale(animationScaleFactor, animationScaleFactor);
                    mCurrentMatrix.postTranslate(animationTransX, animationTransY);
                    ZoomableImageView.this.setImageMatrix(mCurrentMatrix);
                }
            });
//            transXValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                    float[] matrixValues = new float[9];
//                    mCurrentMatrix.getValues(matrixValues);
//                    float currentX = matrixValues[Matrix.MTRANS_X];
//
//                    float newTransX = (float) valueAnimator.getAnimatedValue();
//                    mCurrentMatrix.postTranslate(newTransX - currentX, 0);
//                    ZoomableImageView.this.setImageMatrix(mCurrentMatrix);
//                }
//            });
//            transYValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                    float[] matrixValues = new float[9];
//                    mCurrentMatrix.getValues(matrixValues);
//                    float currentY = matrixValues[Matrix.MTRANS_Y];
//
//                    float newTransY = (float) valueAnimator.getAnimatedValue();
//                    mCurrentMatrix.postTranslate(0, newTransY - currentY);
//                    ZoomableImageView.this.setImageMatrix(mCurrentMatrix);
//                }
//            });
//            animatorSet.playTogether(scaleFactorValueAnimation, transXValueAnimator, transYValueAnimator);
//            animatorSet.start();
            scaleFactorValueAnimation.start();

        }
    }


}