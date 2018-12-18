package barsotti.alejandro.prototipotf.customViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import java.util.Locale;

import barsotti.alejandro.prototipotf.Utils.MathUtils;
import barsotti.alejandro.prototipotf.customInterfaces.IOnTangentPointChangeListener;

public class Angle extends Shape implements IOnTangentPointChangeListener {
    /**
     * Tag utilizado con fines de debug.
     */
    private final String TAG = "Angle";
    /**
     * Número que indica la cantidad de puntos que componen el ángulo. El orden en que se encuentran es:
     * primer extremo, vértice, segundo extremo.
     */
    private static final int NUMBER_OF_POINTS = 3;
    /**
     * Radio del arco del ángulo a dibujar.
     */
    private static final int ARC_RADIUS = 50;
    /**
     * Número que indica la longitud mínima de los segmentos que forman el ángulo.
     */
    private static final int ANGLE_SEGMENT_MIN_LENGTH = ARC_RADIUS * 2;
    /**
     * Medida del ángulo actual representado por la figura.
     */
    private float mSweepAngle;
    /**
     * Medida del ángulo de inicio de la figura (medido desde el eje X positivo hasta el primer segmento del
     * ángulo en sentido horario.
     */
    private float mStartAngle;
    /**
     * Rectángulo que demarca la zona en que se dibujará el arco del ángulo.
     */
    private RectF mArcOval = new RectF();
    /**
     * Pintura utilizada para la representación gráfica del texto que indica la medida del ángulo.
     */
    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private PointF mFirstEnd = new PointF(), mSecondEnd = new PointF(), mVertex = new PointF();
    private Locale mLocale = new Locale("es", "ES");
    private PointF mTextCoordinates;

    public Angle(Context context) {
        this(context, null);
    }

    public Angle(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initializeShape();
        Log.d(TAG, "Constructor.");
    }

    @Override
    protected void initializeShape() {
        // Establecer marca que determina que los parámetros de la figura se calculen constantemente.
        mComputeShapeConstantly = true;

        // Establecer parámetros de la pintura a utilizar para la representación gráfica de la medida del
        // ángulo.
        mTextPaint.setStyle(Paint.Style.STROKE);
        mTextPaint.setColor(mShapePaint.getColor());
        mTextPaint.setTypeface(Typeface.SANS_SERIF);
        mTextPaint.setShadowLayer(5, 0, 0, Color.BLACK);
        mTextPaint.setTextSize(48);
    }

    @Override
    protected int getNumberOfPointsInShape() {
        return NUMBER_OF_POINTS;
    }

    @Override
    protected int getShapeColor() {
        return Color.GREEN;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMappedShapePoints.size() == NUMBER_OF_POINTS) {
            // Dibujar borde del arco.
            canvas.drawArc(mArcOval, mStartAngle, mSweepAngle, false,
                mIsSelected ? mSelectedShapeBorderPaint : mShapeBorderPaint);
            // Dibujar línea principal del arco.
            canvas.drawArc(mArcOval, mStartAngle, mSweepAngle, false,
                mIsSelected ? mSelectedShapePaint : mShapePaint);

            // Dibujar borde del ángulo.
            canvas.drawLine(mFirstEnd.x, mFirstEnd.y, // Primer extremo.
                mVertex.x, mVertex.y, // Vértice.
                mIsSelected ? mSelectedShapeBorderPaint : mShapeBorderPaint);
            canvas.drawLine(mVertex.x, mVertex.y, // Vértice.
                mSecondEnd.x, mSecondEnd.y, // Segundo extremo.
                mIsSelected ? mSelectedShapeBorderPaint : mShapeBorderPaint);

            // Dibujar línea principal del ángulo.
            canvas.drawLine(mFirstEnd.x, mFirstEnd.y, // Primer extremo.
                mVertex.x, mVertex.y, // Vértice.
                mIsSelected ? mSelectedShapePaint : mShapePaint);
            canvas.drawLine(mVertex.x, mVertex.y, // Vértice.
                mSecondEnd.x, mSecondEnd.y, // Segundo extremo.
                mIsSelected ? mSelectedShapePaint : mShapePaint);

            // Dibujar texto que indica la medida del Ángulo.
//            canvas.drawText(String.format(mLocale, "%.2fº", mSweepAngle), mVertex.x + 25, mVertex.y + 25, mTextPaint);
            canvas.drawText(String.format(mLocale, "%.2fº", mSweepAngle), mTextCoordinates.x - 75,
                mTextCoordinates.y, mTextPaint);
        }

        super.onDraw(canvas);
    }

    @Override
    public float computeDistanceBetweenTouchAndShape(PointF point) {
        if (mMappedShapePoints.size() != NUMBER_OF_POINTS) {// Figura completa.
            return -1;
        }

        float distanceFromSegment1ToPoint = MathUtils.distanceBetweenSegmentAndPoint(point,
            mMappedShapePoints.get(0), mMappedShapePoints.get(1));

        float distanceFromSegment2ToPoint = MathUtils.distanceBetweenSegmentAndPoint(point,
            mMappedShapePoints.get(1), mMappedShapePoints.get(2));

        float minDistance = Math.min(distanceFromSegment1ToPoint, distanceFromSegment2ToPoint);

        return minDistance <= TOUCH_RADIUS ? minDistance : -1;
    }

    @Override
    protected void computeShape() {
        if (mShapePoints.size() == NUMBER_OF_POINTS) {
            mSweepAngle = MathUtils.calculateSweepAngleFromThreePoints(mShapePoints);
            mStartAngle = MathUtils.calculateStartAngleFromThreePoints(mShapePoints);
        }
    }

    @Override
    public void updateViewMatrix(Matrix matrix) {
        super.updateViewMatrix(matrix);

        // Mapear las variables propias del ángulo según la nueva matriz.
        mMappedShapePoints = mapPoints(mCurrentMatrix, mShapePoints);

        if (mMappedShapePoints.size() == NUMBER_OF_POINTS) {
            mVertex = mMappedShapePoints.get(1);
            mFirstEnd = mMappedShapePoints.get(0);
            mFirstEnd = MathUtils.extendEndPointToDistance(mVertex, mFirstEnd, ANGLE_SEGMENT_MIN_LENGTH, true);
            mSecondEnd = mMappedShapePoints.get(2);
            mSecondEnd = MathUtils.extendEndPointToDistance(mVertex, mSecondEnd, ANGLE_SEGMENT_MIN_LENGTH, true);

            mTextCoordinates = MathUtils.getCoordinatesForTextDrawing(mVertex, mFirstEnd, mSecondEnd);

            mArcOval.set(mVertex.x - ARC_RADIUS, mVertex.y - ARC_RADIUS, mVertex.x + ARC_RADIUS,
                mVertex.y + ARC_RADIUS);
        }

        invalidate();
    }

    @Override
    public void updateTangentPoints(PointF tangentPoint, PointF linePoint, PointF circleCenterPoint) {
        mShapePoints.clear();

        // Establecer primer extremo del ángulo (ubicado sobre la línea tangente).
        mShapePoints.add(MathUtils.extendEndPointToDistance(tangentPoint, linePoint,
            ANGLE_SEGMENT_MIN_LENGTH, false));

        // Establecer vértice del ángulo (punto tangente).
        mShapePoints.add(new PointF(tangentPoint.x, tangentPoint.y));

        // Establecer segundo extremo del ángulo (ubicado sobre la recta radial de la tangente).
        mShapePoints.add(MathUtils.extendEndPointToDistance(tangentPoint, circleCenterPoint,
            ANGLE_SEGMENT_MIN_LENGTH, false));

        computeShape();
        updateViewMatrix(null);
    }
}
