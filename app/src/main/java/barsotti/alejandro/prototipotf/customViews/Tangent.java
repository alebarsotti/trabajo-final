package barsotti.alejandro.prototipotf.customViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;

import barsotti.alejandro.prototipotf.Utils.MathUtils;
import barsotti.alejandro.prototipotf.customInterfaces.IOnCircumferenceCenterChangeListener;
import barsotti.alejandro.prototipotf.customInterfaces.IOnTangentPointChangeListener;
import barsotti.alejandro.prototipotf.customInterfaces.ITangent;

import static barsotti.alejandro.prototipotf.Utils.MathUtils.MAX_NUMBER_OF_POINTS_PER_LINE;

public class Tangent extends Shape implements IOnCircumferenceCenterChangeListener, ITangent {
    //region Constantes
    /**
     * Tag utilizado con fines de debug.
     */
    private static final String TAG = "Tangent";
    /**
     * Número que indica la cantidad de puntos que componen la tangente.
     */
    private static final int NUMBER_OF_POINTS = 1;
    //endregion

    //region Propiedades
    /**
     * Punto que indica las coordenadas del centro de la circunferencia de la cual depende la tangente.
     */
    private PointF mCircumferenceCenter;
    /**
     * Punto que indica las coordenadas del centro de la circunferencia de la cual depende la tangente
     * mapeado según la matriz actual.
     */
    private PointF mMappedCircumferenceCenter;
    /**
     * Número que indica la medida del radio de la circunferencia de la cual depende la tangente.
     */
    private float mCircumferenceRadius;
    /**
     * Lista de puntos utilizados para representar la línea tangente.
     */
    private float[] mTangentPoints = new float[4];
    /**
     * Lista de puntos utilizados para representar la línea tangente mapeados según la matriz actual.
     */
    private float[] mMappedTangentPoints = new float[4];
    /**
     * Lista de puntos utilizados para representar la recta radial que acompaña a la tangente.
     */
    private float[] mPathPoints = new float[MAX_NUMBER_OF_POINTS_PER_LINE * 2];
    /**
     * Lista de puntos utilizados para representar la recta radial que acompaña a la tangente mapeados según
     * la matriz actual.
     */
    private float[] mMappedPathPoints = new float[MAX_NUMBER_OF_POINTS_PER_LINE * 2];
    /**
     * Lista de suscriptores a eventos de actualización de la circunferencia.
     */
    private ArrayList<IOnTangentPointChangeListener> mListeners = new ArrayList<>();
    //endregion

    //region Constructores
    public Tangent(Context context) {
        this(context, null);
    }

    public Tangent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initializeShape();
        Log.d(TAG, "Constructor.");
    }
    //endregion

    @Override
    protected void onDraw(Canvas canvas) {
        // Dibujar si la figura se encuentra completa.
        if (mMappedTangentPoints != null) {
            // Dibujar recta radial.
            canvas.drawLines(mMappedPathPoints, mIsSelected ? mSelectedShapeBorderPaint : mShapeBorderPaint);
            canvas.drawLines(mMappedPathPoints, mIsSelected ? mSelectedShapePaint : mShapePaint);

            // Dibujar tangente.
            canvas.drawLines(mMappedTangentPoints,
                mIsSelected ? mSelectedShapeBorderPaint : mShapeBorderPaint);
            canvas.drawLines(mMappedTangentPoints, mIsSelected ? mSelectedShapePaint : mShapePaint);
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
        /*
        En caso de que el centro de la circunferencia de la cual depende la tangente sea nulo o de que la
        figura no se encuentre completa, no calcular distancia.
         */
        if (mMappedCircumferenceCenter == null || mMappedShapePoints.size() != NUMBER_OF_POINTS) {
            return -1;
        }

        // Distancia entre el punto y la recta radial.
        float distanceFromRadialLineToPoint = MathUtils.distanceBetweenSegmentAndPoint(point,
            mMappedCircumferenceCenter, mMappedShapePoints.get(0));

        // Distancia entre el punto y la recta tangente.
        float distanceFromTangentToPoint = MathUtils.distanceBetweenLineAndPoint(point,
                    new PointF(mMappedTangentPoints[0], mMappedTangentPoints[1]),
                    new PointF(mMappedTangentPoints[2], mMappedTangentPoints[3]));

        float minDistance = Math.min(distanceFromRadialLineToPoint, distanceFromTangentToPoint);

        return minDistance <= TOUCH_TOLERANCE ? minDistance : -1;
    }

    @Override
    protected void computeShape() {
        if (mShapePoints.size() == NUMBER_OF_POINTS) {
            // Trasladar el punto marcado hasta la circunferencia.
            PointF pointInCircumference = MathUtils.translatePointToCircumference(mCircumferenceCenter,
                mCircumferenceRadius, mShapePoints.get(0));
            mShapePoints.set(0, pointInCircumference);
            // Calcular dos puntos que definen la tangente.
            mTangentPoints = MathUtils.tangentToCircumference(mCircumferenceCenter, pointInCircumference,
                mCircumferenceRadius);
            // Calcular puntos utilizados para representar la recta radial.
            mPathPoints = MathUtils.pointArrayFromLine(mCircumferenceCenter, pointInCircumference);
        }

        // Informar a los suscriptores sobre el cambio en la figura.
        triggerOnTangentPointChangeListener();
    }

    @Override
    public void updateViewMatrix(Matrix matrix) {
        super.updateViewMatrix(matrix);

        // Mapear las variables propias de la tangente según la nueva matriz.
        mMappedCircumferenceCenter = mapPoint(mCurrentMatrix, mCircumferenceCenter);
        mCurrentMatrix.mapPoints(mMappedTangentPoints, mTangentPoints);
        mMappedShapePoints = mapPoints(mCurrentMatrix, mShapePoints);
        mMappedPathPoints = new float[mPathPoints.length];
        mCurrentMatrix.mapPoints(mMappedPathPoints, mPathPoints);

        invalidate();
    }

    @Override
    public void updateCircumferenceCenterAndRadius(PointF center, float radius) {
        mCircumferenceCenter = new PointF(center.x, center.y);
        mCircumferenceRadius = radius;
        computeShape();
    }

    //region Administración de suscriptores
    @Override
    public void addOnTangentPointChangeListener(IOnTangentPointChangeListener listener) {
        mListeners.add(listener);
        if (mShapePoints.size() > 0) {
            listener.updateTangentPoints(mShapePoints.get(0),
                new PointF(mTangentPoints[0], mTangentPoints[1]), mCircumferenceCenter);
        }
    }

    private void triggerOnTangentPointChangeListener() {
        if (mShapePoints.size() > 0) {
            for (IOnTangentPointChangeListener listener: mListeners) {
                listener.updateTangentPoints(mShapePoints.get(0),
                    new PointF(mTangentPoints[0], mTangentPoints[1]), mCircumferenceCenter);
            }
        }
    }

    @Override
    public void removeOnTangentPointChangeListener(IOnTangentPointChangeListener listener) {
        mListeners.remove(listener);
    }
    //endregion
}