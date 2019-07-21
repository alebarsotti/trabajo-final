package barsotti.alejandro.tf.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;

import barsotti.alejandro.tf.utils.MathUtils;
import barsotti.alejandro.tf.interfaces.IOnCartesianAxesPointChangeListener;
import barsotti.alejandro.tf.interfaces.ICartesianAxes;


public class CartesianAxes extends Shape implements ICartesianAxes {

    //region Constantes
    /**
     * Tag utilizado con fines de debug.
     */
    private static final String TAG = "CartesianAxes";
    /**
     * Número que indica la cantidad de puntos que componen los ejes cartesianos.
     */
    private static final int NUMBER_OF_POINTS = 3;
    //endregion

    //region Propiedades
    /**
     * Lista de puntos utilizados para representar los ejes cartesianos. Los dos primeros definen el Eje X,
     * los dos segundos, el Eje Y.
     */
    private float[] CartesianAxesPoints = new float[8];
    /**
     * Lista de puntos utilizados para representar los ejes cartesianos mapeados según la matriz actual.
     * Los dos primeros definen el Eje X, los dos segundos, el Eje Y.
     */
    private float[] MappedCartesianAxesPoints = new float[8];
    /**
     * Lista de suscriptores a eventos de actualización de los ejes cartesianos.
     */
    private ArrayList<IOnCartesianAxesPointChangeListener> mListeners = new ArrayList<>();
    //endregion

    //region Constructores
    public CartesianAxes(Context context) {
        this(context, null);
    }

    public CartesianAxes(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initializeShape();
        Log.d(TAG, "Constructor.");
    }
    //endregion

    @Override
    protected void onDraw(Canvas canvas) {
        // Dibujar si la figura se encuentra completa.
        if (MappedCartesianAxesPoints != null) {
            // Dibujar ejes.
            canvas.drawLines(MappedCartesianAxesPoints,
                mIsSelected ? mSelectedShapeBorderPaint : mShapeBorderPaint);
            canvas.drawLines(MappedCartesianAxesPoints, mIsSelected ? mSelectedShapePaint : mShapePaint);
        }

        super.onDraw(canvas);
    }

    @Override
    protected void initializeShape() {
    }

    @Override
    protected int getNumberOfPointsInShape() {
        return NUMBER_OF_POINTS;
    }

    @Override
    protected int getShapeColor() {
        return Color.CYAN;
    }

    @Override
    public float computeDistanceBetweenTouchAndShape(PointF point) {
        // En caso de que la figura no se encuentre completa, no calcular distancia.
        if (mMappedShapePoints.size() != NUMBER_OF_POINTS) {
            return -1;
        }

        // Distancia entre el punto y cada eje.
        float distanceFromAxisXToPoint = MathUtils.distanceBetweenLineAndPoint(point,
                    new PointF(MappedCartesianAxesPoints[0], MappedCartesianAxesPoints[1]),
                    new PointF(MappedCartesianAxesPoints[2], MappedCartesianAxesPoints[3]));

        float distanceFromAxisYToPoint = MathUtils.distanceBetweenLineAndPoint(point,
                    new PointF(MappedCartesianAxesPoints[4], MappedCartesianAxesPoints[5]),
                    new PointF(MappedCartesianAxesPoints[6], MappedCartesianAxesPoints[7]));

        float minDistance = Math.min(distanceFromAxisXToPoint, distanceFromAxisYToPoint);

        return minDistance <= TOUCH_TOLERANCE ? minDistance : -1;
    }

    @Override
    protected void computeShape() {
        if (mShapePoints.size() == NUMBER_OF_POINTS) {
//            // Trasladar el punto marcado hasta la circunferencia.
//            PointF pointInCircumference = translatePointToCircumference(mCircumferenceCenter,
//                mCircumferenceRadius, mShapePoints.get(0));
//            mShapePoints.set(0, pointInCircumference);
//            // Calcular dos puntos que definen la tangente.
//            CartesianAxesPoints = tangentToCircumference(mCircumferenceCenter, pointInCircumference,
//                mCircumferenceRadius);
//            // Calcular puntos utilizados para representar la recta radial.
//            mPathPoints = MathUtils.pointArrayFromLine(mCircumferenceCenter, pointInCircumference);

            CartesianAxesPoints = MathUtils.calculateCartesianAxesFromThreePoints(mShapePoints.get(0),
                mShapePoints.get(1), mShapePoints.get(2));

        }

        // Informar a los suscriptores sobre el cambio en la figura.
        triggerOnCartesianAxesPointChangeListener();
    }

    @Override
    public void updateViewMatrix(Matrix matrix) {
        super.updateViewMatrix(matrix);

        // Mapear las variables propias de los ejes cartesianos según la nueva matriz.
        mCurrentMatrix.mapPoints(MappedCartesianAxesPoints, CartesianAxesPoints);
        mMappedShapePoints = mapPoints(mCurrentMatrix, mShapePoints);

        invalidate();
    }

    //region Administración de suscriptores
    @Override
    public void addOnCartesianAxesPointChangeListener(IOnCartesianAxesPointChangeListener listener) {
        mListeners.add(listener);
        if (mShapePoints.size() == NUMBER_OF_POINTS) {
            listener.updateCartesianAxesPoints(CartesianAxesPoints,
                new PointF(mShapePoints.get(1).x, mShapePoints.get(1).y));
        }
    }

    private void triggerOnCartesianAxesPointChangeListener() {
        if (mShapePoints.size() == NUMBER_OF_POINTS) {
            for (IOnCartesianAxesPointChangeListener listener: mListeners) {
                listener.updateCartesianAxesPoints(CartesianAxesPoints,
                    new PointF(mShapePoints.get(1).x, mShapePoints.get(1).y));
            }
        }
    }

    @Override
    public void removeOnCartesianAxesPointChangeListener(IOnCartesianAxesPointChangeListener listener) {
        mListeners.remove(listener);
    }
    //endregion
}