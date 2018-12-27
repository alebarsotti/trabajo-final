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
import barsotti.alejandro.prototipotf.customInterfaces.IOnCircleCenterChangeListener;
import barsotti.alejandro.prototipotf.customInterfaces.IOnTangentPointChangeListener;
import barsotti.alejandro.prototipotf.customInterfaces.ITangent;

import static barsotti.alejandro.prototipotf.Utils.MathUtils.MAX_NUMBER_OF_POINTS_PER_LINE;

public class Tangent extends Shape implements IOnCircleCenterChangeListener, ITangent {
    // Tag utilizado con fines de debug.
    private static final String TAG = "Tangent";
    // Número que indica la cantidad de puntos que componen la tangente.
    private static final int NUMBER_OF_POINTS = 1;
    // Punto que indica las coordenadas del centro de la circunferencia de la que depende la tangente.
    private PointF mCircleCenter;
    private float mCircleRadius;
    // Punto que indica las coordenadas del centro de la circunferencia de la que depende la tangente
    // mapeado según la matriz actual.
    private PointF mMappedCircleCenter;
    // Lista de puntos utilizados para representar las líneas que componen la tangente (tangente + línea
    // radial)
    private float[] mTangentPoints = new float[4];
    // Lista de puntos utilizados para representar las líneas que componen la tangente (tangente + línea
    // radial) mapeados según la matriz actual.
    private float[] mMappedTangentPoints = new float[4];
    /**
     * Pintura utilizada para la representación gráfica de la recta radial que acompaña a la tangente.
     */
    private Paint mRadialLinePaint;

    /**
     * Pintura utilizada para la representación gráfica del borde de la recta radial que acompaña a la
     * tangente.
     */
    private Paint mRadialLineBorderPaint;
    private Paint mSelectedRadialLinePaint;
    private Paint mSelectedRadialLineBorderPaint;
    private float[] mPathPoints = new float[MAX_NUMBER_OF_POINTS_PER_LINE * 2];
    private float[] mMappedPathPoints = new float[MAX_NUMBER_OF_POINTS_PER_LINE * 2];
    // Lista de suscriptores a eventos de actualización de la circunferencia.
    private ArrayList<IOnTangentPointChangeListener> mListeners = new ArrayList<>();

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
//            for (PointF pointToDraw: mMappedShapePoints) {
//                // Borde
//                canvas.drawLine(mMappedCircleCenter.x, mMappedCircleCenter.y, pointToDraw.x, pointToDraw.y,
//                    mIsSelected ? mSelectedRadialLineBorderPaint : mRadialLineBorderPaint);
//                // Línea principal.
//                canvas.drawLine(mMappedCircleCenter.x, mMappedCircleCenter.y, pointToDraw.x, pointToDraw.y,
//                    mIsSelected ? mSelectedRadialLinePaint : mRadialLinePaint);
//            }

            canvas.drawLines(mMappedPathPoints, mIsSelected ? mSelectedRadialLineBorderPaint : mRadialLineBorderPaint);
            canvas.drawLines(mMappedPathPoints, mIsSelected ? mSelectedRadialLinePaint : mRadialLinePaint);



            // Dibujar tangente.
            // Borde.
            canvas.drawLines(mMappedTangentPoints, mIsSelected ?
                mSelectedShapeBorderPaint : mShapeBorderPaint);
            // Línea principal.
            canvas.drawLines(mMappedTangentPoints, mIsSelected ? mSelectedShapePaint : mShapePaint);
        }

//        // Dibujar puntos solo si la figura está seleccionada.
//        if (mIsSelected) {
//            for (PointF pointToDraw: mMappedShapePoints) {
//                // Dibujar relleno del área de control del punto.
//                canvas.drawCircle(pointToDraw.x, pointToDraw.y, mPointRadius, mPointPaint);
//                // Dibujar borde del área de control del punto.
//                canvas.drawCircle(pointToDraw.x, pointToDraw.y, mPointRadius, mPointBorderPaint);
//                // Dibujar punto central del área de control del punto.
//                canvas.drawCircle(pointToDraw.x, pointToDraw.y, CENTER_POINT_RADIUS, mCenterPointPaint);
//            }
//        }
        super.onDraw(canvas);
    }

    @Override
    protected void initializeShape() {
        // Establecer parámetros de la pintura a utilizar para la representación gráfica de la recta radial.
//        DashPathEffect dashPathEffect = new DashPathEffect(new float[]{10, 20}, 0);
        mRadialLinePaint = new Paint(mShapePaint);
//        mRadialLinePaint.setPathEffect(dashPathEffect);
//        mRadialLinePaint.setAlpha(mRadialLinePaint.getAlpha() / 2);

        mRadialLineBorderPaint = new Paint(mShapeBorderPaint);
//        mRadialLineBorderPaint.setPathEffect(dashPathEffect);
//        mRadialLineBorderPaint.setAlpha(mRadialLineBorderPaint.getAlpha() / 2);

        mSelectedRadialLinePaint = new Paint(mSelectedShapePaint);
//        mSelectedRadialLinePaint.setPathEffect(dashPathEffect);
//        mSelectedRadialLinePaint.setAlpha(mSelectedRadialLinePaint.getAlpha() / 2);

        mSelectedRadialLineBorderPaint = new Paint(mSelectedShapeBorderPaint);
//        mSelectedRadialLineBorderPaint.setPathEffect(dashPathEffect);
//        mSelectedRadialLineBorderPaint.setAlpha(mSelectedRadialLineBorderPaint.getAlpha() / 2);
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
        if (mMappedCircleCenter == null || // Centro de circunferencia no nulo.
            mMappedShapePoints.size() != NUMBER_OF_POINTS) {// Figura completa.
            return -1;
        }

        // Distancia entre el punto y la recta radial.
        float distanceFromRadialLineToPoint = MathUtils.distanceBetweenSegmentAndPoint(point,
            mMappedCircleCenter, mMappedShapePoints.get(0));

        // Distancia de punto hasta recta tangente.
        float distanceFromTangentToPoint = MathUtils.distanceBetweenLineAndPoint(point,
                    new PointF(mMappedTangentPoints[0], mMappedTangentPoints[1]),
                    new PointF(mMappedTangentPoints[2], mMappedTangentPoints[3]));

        float minDistance = Math.min(distanceFromRadialLineToPoint, distanceFromTangentToPoint);

        return minDistance <= TOUCH_RADIUS ? minDistance : -1;
    }

    @Override
    protected void computeShape() {
        if (mShapePoints.size() == NUMBER_OF_POINTS) {
            PointF pointInCircumference = MathUtils.translatePointToCircumference(mCircleCenter,
                mCircleRadius, mShapePoints.get(0));
            mShapePoints.set(0, pointInCircumference);
            mTangentPoints = MathUtils.tangentToCircumference(mCircleCenter, pointInCircumference, mCircleRadius);
            mPathPoints = MathUtils.pointArrayFromLine(mCircleCenter, pointInCircumference);
        }
        triggerOnTangentPointChangeListener();
    }

    @Override
    public void updateViewMatrix(Matrix matrix) {
        super.updateViewMatrix(matrix);

        mMappedCircleCenter = mapPoint(mCurrentMatrix, mCircleCenter);
        mCurrentMatrix.mapPoints(mMappedTangentPoints, mTangentPoints);
        mMappedShapePoints = mapPoints(mCurrentMatrix, mShapePoints);
        mMappedPathPoints = new float[mPathPoints.length];
        mCurrentMatrix.mapPoints(mMappedPathPoints, mPathPoints);

        invalidate();
    }

    @Override
    public void updateCircleCenterAndRadius(PointF center, float radius) {
        mCircleCenter = new PointF(center.x, center.y);
        mCircleRadius = radius;
        computeShape();
    }

    @Override
    public void addOnTangentPointChangeListener(IOnTangentPointChangeListener listener) {
        mListeners.add(listener);
//        triggerOnTangentPointChangeListener();
        if (mShapePoints.size() > 0) {
            listener.updateTangentPoints(mShapePoints.get(0),
                new PointF(mTangentPoints[0], mTangentPoints[1]), mCircleCenter);
        }
    }

    private void triggerOnTangentPointChangeListener() {
        if (mShapePoints.size() > 0) {
            for (IOnTangentPointChangeListener listener: mListeners) {
                listener.updateTangentPoints(mShapePoints.get(0),
                    new PointF(mTangentPoints[0], mTangentPoints[1]), mCircleCenter);
            }
        }
    }

    @Override
    public void removeOnTangentPointChangeListener(IOnTangentPointChangeListener listener) {
        mListeners.remove(listener);
//        triggerOnTangentPointChangeListener();
    }
}