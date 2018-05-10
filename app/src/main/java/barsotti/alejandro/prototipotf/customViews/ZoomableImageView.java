package barsotti.alejandro.prototipotf.customViews;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.animation.AccelerateDecelerateInterpolator;

public class ZoomableImageView extends android.support.v7.widget.AppCompatImageView {
    private static final int MAX_ZOOM_SCALE = 15;
    private static final int ANIMATION_DURATION = 250;

    private boolean mIsScrolling;
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
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.performClick();

        // Informar del evento a los detectores de gestos y escala.
        mScaleGestureDetector.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);

        // Verificar necesidad de desplazamiento al finalizar un scroll.
        if (event.getAction() == MotionEvent.ACTION_UP && mIsScrolling) {
            mIsScrolling = false;
            handleScrollEnded();
        }

        this.setImageMatrix(mCurrentMatrix);

        return true;
    }

    private void handleScrollEnded() {
        // Verificar que no se vean zonas negras fuera de la imagen. En tal caso, ajustar el desplazamiento.
        final float[] matrixValues = new float[9];
        mCurrentMatrix.getValues(matrixValues);

        // Si el factor de escala es menor al mínimo, no realizar ninguna acción.
        if (matrixValues[Matrix.MSCALE_X] < mMinScaleFactor) {
            return;
        }

        // Desplazamiento actual.
        final float transX = matrixValues[Matrix.MTRANS_X];
        final float transY = matrixValues[Matrix.MTRANS_Y];

        // Dimensiones actuales de la imagen.
        float bitmapWidth = mBitmapWidth * matrixValues[Matrix.MSCALE_X];
        float bitmapHeight = mBitmapHeight * matrixValues[Matrix.MSCALE_Y];

        // Dimensiones de la view.
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        // Límites de desplazamiento.
        float dX = viewWidth - bitmapWidth;
        float minTransX = dX < 0 ? dX : 0;
        float maxTransX = dX < 0 ? 0 : dX;

        float dY = viewHeight - bitmapHeight;
        float minTransY = dY < 0 ? dY : 0;
        float maxTransY = dY < 0 ? 0 : dY;

        float deltaTransX = 0;
        float deltaTransY = 0;
        // Si el desplazamiento no se encuentra dentro de los límites permitidos. Calcular delta.
        if (transX > maxTransX || transX < minTransX) {
            float limitX = transX > maxTransX ? maxTransX : minTransX;
            deltaTransX = limitX - transX;
        }
        // Si el ancho de la imagen es menor al ancho de la pantalla, centrar horizontalmente.
        if (viewWidth > bitmapWidth) {
            deltaTransX = (viewWidth - bitmapWidth) / 2 - transX;
        }

        // Si el desplazamiento no se encuentra dentro de los límites permitidos. Calcular delta.
        if (transY > maxTransY || transY < minTransY) {
            float limitY = transY > maxTransY ? maxTransY : minTransY;
            deltaTransY = limitY - transY;
        }
        // Si la altura de la imagen es menor a la altura de la pantalla, centrar verticalmente.
        if (viewHeight > bitmapHeight) {
            deltaTransY = (viewHeight - bitmapHeight) / 2 - transY;
        }

        // Si no es necesario desplazar la matriz, no realizar ninguna acción.
        if (deltaTransX == 0 && deltaTransY == 0) {
            return;
        }

        // Desplazar la matriz.
        // mCurrentMatrix.postTranslate(deltaTransX, deltaTransY);
        ValueAnimator transValueAnimator = ValueAnimator.ofFloat(0, 1);
        transValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        transValueAnimator.setDuration(ANIMATION_DURATION);
        final float finalDeltaTransX = deltaTransX;
        final float finalDeltaTransY = deltaTransY;
        final float currentScale = matrixValues[Matrix.MSCALE_X];
        transValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator.getAnimatedFraction();
                float animatedTransX = transX + (finalDeltaTransX * animatedFraction);
                float animatedTransY = transY + (finalDeltaTransY * animatedFraction);
                mCurrentMatrix.setScale(currentScale, currentScale);
                mCurrentMatrix.postTranslate(animatedTransX, animatedTransY);

                ZoomableImageView.this.setImageMatrix(mCurrentMatrix);
            }
        });
        transValueAnimator.start();
    }

    //region Setters
    public void setScale(int displayWidth, int displayHeight) {
        // Establecer variables de tamaño de la imagen.
        Drawable drawable = this.getDrawable();
        mBitmapWidth = drawable.getIntrinsicWidth();
        mBitmapHeight = drawable.getIntrinsicHeight();

        // Escalar y centrar matriz de la view que contiene la imagen.
        RectF dest = new RectF(0, 0, displayWidth, displayHeight);
        RectF src = new RectF(0, 0, mBitmapWidth, mBitmapHeight);
        mCurrentMatrix.setRectToRect(src, dest, Matrix.ScaleToFit.CENTER);
        mDefaultMatrix.set(mCurrentMatrix);

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
        mDefaultMatrix = new Matrix();
        mCurrentMatrix = new Matrix();
        mIsScrolling = false;

        // Inicializar detectores de gestos y escala.
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ZoomableImageViewScaleListener());
        mGestureDetector = new GestureDetector(getContext(), new ZoomableImageViewGestureListener());
    }
    //endregion

    private class ZoomableImageViewGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (!mCurrentMatrix.equals(mDefaultMatrix)) {
                // La imagen debe volver a su estado original (mDefaultMatrix).
                // Obtener valores finales (default) de la animación.
                float[] defaultMatrixValues = new float[9];
                mDefaultMatrix.getValues(defaultMatrixValues);
                float finalScaleFactor = defaultMatrixValues[Matrix.MSCALE_X];
                float finalTransX = defaultMatrixValues[Matrix.MTRANS_X];
                float finalTransY = defaultMatrixValues[Matrix.MTRANS_Y];

                // Obtener valores iniciales (actuales) de la animación.
                float[] currentMatrixValues = new float[9];
                mCurrentMatrix.getValues(currentMatrixValues);
                float currentScaleFactor = currentMatrixValues[Matrix.MSCALE_X];
                float currentTransX = currentMatrixValues[Matrix.MTRANS_X];
                float currentTransY = currentMatrixValues[Matrix.MTRANS_Y];

                // Animar - ValueAnimator
                mCurrentMatrix.reset();
                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator.setDuration(ANIMATION_DURATION);
                valueAnimator.addUpdateListener(new ZoomAnimatorUpdateListener(currentScaleFactor,
                    currentTransX, currentTransY, finalScaleFactor, finalTransX, finalTransY));
                valueAnimator.addListener(new ZoomAnimatorListener());
                valueAnimator.start();
            }
            else {
                float scale = mMinScaleFactor * MAX_ZOOM_SCALE / 4;
                final float currentScale = mMinScaleFactor;
                final float deltaScale = scale - currentScale;

                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                valueAnimator.setDuration(ANIMATION_DURATION);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    private float mLastScaleFactor = currentScale;
                    private final PointF pivot = new PointF(getWidth() / 2, getHeight() / 2);

                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float animatedFraction = valueAnimator.getAnimatedFraction();
                        float currentScaleFactor = currentScale + deltaScale * animatedFraction;
                        float deltaScaleFactor = currentScaleFactor / mLastScaleFactor;
                        mLastScaleFactor = mLastScaleFactor * deltaScaleFactor;

                        mCurrentMatrix.postScale(deltaScaleFactor, deltaScaleFactor, pivot.x, pivot.y);
                        ZoomableImageView.this.setImageMatrix(mCurrentMatrix);
                    }
                });
                valueAnimator.start();
            }

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Establecer estado de scroll.
            mIsScrolling = true;

            // Desplazar matriz. Las distancias se invierten para que sean correctas.
            mCurrentMatrix.postTranslate(-distanceX, -distanceY);

            return true;
        }
    }

    private class ZoomableImageViewScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();

            // Obtener factor de escala actual.
            float[] matrixValues = new float[9];
            mCurrentMatrix.getValues(matrixValues);
            float currentScaleFactor = matrixValues[Matrix.MSCALE_X];

            // Forzar que el factor de escala sea menor o igual al factor de escala máximo.
            if (currentScaleFactor * scaleFactor > mMaxScaleFactor) {
                scaleFactor = mMaxScaleFactor / currentScaleFactor;
            }


            PointF focus = new PointF(detector.getFocusX(), detector.getFocusY());
            // Escalar matriz.
            mCurrentMatrix.postScale(scaleFactor, scaleFactor, focus.x, focus.y);

            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

            // FIXME: Si se hace zoom- y se scrollea al mismo tiempo, la imagen queda chica.
            // Verificar que el factor de escala final se encuentre dentro del rango permitido.
            float[] initialMatrixValues = new float[9];
            mCurrentMatrix.getValues(initialMatrixValues);
            final float initialScaleFactor = initialMatrixValues[Matrix.MSCALE_X];

            // Si el factor de escala es mayor al mínimo establecido, no llevar a cabo ninguna acción.
            if (initialScaleFactor > mMinScaleFactor) {
                return;
            }

            // Determinar valores iniciales y finales para la animación.
            float initialTransX = initialMatrixValues[Matrix.MTRANS_X];
            float initialTransY = initialMatrixValues[Matrix.MTRANS_Y];

            float[] defaultMatrixValues = new float[9];
            mDefaultMatrix.getValues(defaultMatrixValues);
            float finalScaleFactor = mMinScaleFactor;
            float finalTransX = defaultMatrixValues[Matrix.MTRANS_X];
            float finalTransY = defaultMatrixValues[Matrix.MTRANS_Y];

            // Animar - ValueAnimator
            mCurrentMatrix.reset();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator.setDuration(ANIMATION_DURATION);
            valueAnimator.addUpdateListener(new ZoomAnimatorUpdateListener(initialScaleFactor, initialTransX,
                initialTransY, finalScaleFactor, finalTransX, finalTransY));
            valueAnimator.addListener(new ZoomAnimatorListener());
            valueAnimator.start();
        }
    }

    public class ZoomAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        private float mInitialScaleFactor;
        private float mInitialTransX;
        private float mInitialTransY;
        private float mDeltaScaleFactor;
        private float mDeltaTransX;
        private float mDeltaTransY;

        ZoomAnimatorUpdateListener(float initialScaleFactor, float initialTransX, float initialTransY,
                                   float finalScaleFactor, float finalTransX, float finalTransY) {
            this.mInitialScaleFactor = initialScaleFactor;
            this.mInitialTransX = initialTransX;
            this.mInitialTransY = initialTransY;
            this.mDeltaScaleFactor = finalScaleFactor - initialScaleFactor;
            this.mDeltaTransX = finalTransX - initialTransX;
            this.mDeltaTransY = finalTransY - initialTransY;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float animatedFraction = valueAnimator.getAnimatedFraction();

            float animationScaleFactor = mInitialScaleFactor + mDeltaScaleFactor * animatedFraction;
            float animationTransX = mInitialTransX + mDeltaTransX * animatedFraction;
            float animationTransY = mInitialTransY + mDeltaTransY * animatedFraction;

            mCurrentMatrix.setScale(animationScaleFactor, animationScaleFactor);
            mCurrentMatrix.postTranslate(animationTransX, animationTransY);

            ZoomableImageView.this.setImageMatrix(mCurrentMatrix);
        }
    }

    public class ZoomAnimatorListener implements ValueAnimator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animator) {}

        @Override
        public void onAnimationEnd(Animator animator) {
            mCurrentMatrix.set(mDefaultMatrix);
        }

        @Override
        public void onAnimationCancel(Animator animator) {}

        @Override
        public void onAnimationRepeat(Animator animator) {}
    }
}