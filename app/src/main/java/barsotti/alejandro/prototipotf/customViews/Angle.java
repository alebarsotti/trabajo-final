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

import barsotti.alejandro.prototipotf.Utils.MathUtils;

public class Angle extends Shape {
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
            canvas.drawLine(mMappedShapePoints.get(0).x, mMappedShapePoints.get(0).y, // Primer extremo.
                mMappedShapePoints.get(1).x, mMappedShapePoints.get(1).y, // Vértice.
                mIsSelected ? mSelectedShapeBorderPaint : mShapeBorderPaint);
            canvas.drawLine(mMappedShapePoints.get(1).x, mMappedShapePoints.get(1).y, // Vértice.
                mMappedShapePoints.get(2).x, mMappedShapePoints.get(2).y, // Segundo extremo.
                mIsSelected ? mSelectedShapeBorderPaint : mShapeBorderPaint);

            // Dibujar línea principal del ángulo.
            canvas.drawLine(mMappedShapePoints.get(0).x, mMappedShapePoints.get(0).y, // Primer extremo.
                mMappedShapePoints.get(1).x, mMappedShapePoints.get(1).y, // Vértice.
                mIsSelected ? mSelectedShapePaint : mShapePaint);
            canvas.drawLine(mMappedShapePoints.get(1).x, mMappedShapePoints.get(1).y, // Vértice.
                mMappedShapePoints.get(2).x, mMappedShapePoints.get(2).y, // Segundo extremo.
                mIsSelected ? mSelectedShapePaint : mShapePaint);

            // Dibujar texto que indica la medida del Ángulo.
            canvas.drawText(String.valueOf(mSweepAngle), mMappedShapePoints.get(1).x + 25,
                mMappedShapePoints.get(1).y + 25, mTextPaint);
        }

        super.onDraw(canvas);
    }

    @Override
    public boolean checkTouchToSelect(PointF point) {
        // TODO: completar.

        return
            mMappedShapePoints.size() == NUMBER_OF_POINTS && // Figura completa.

                (MathUtils.distanceBetweenSegmentAndPoint(point, mMappedShapePoints.get(0),
                mMappedShapePoints.get(1)) < TOUCH_RADIUS
                ||
                MathUtils.distanceBetweenSegmentAndPoint(point, mMappedShapePoints.get(1),
                    mMappedShapePoints.get(2)) < TOUCH_RADIUS);
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
            PointF vertex = mMappedShapePoints.get(1);
            mArcOval.set(vertex.x - ARC_RADIUS, vertex.y - ARC_RADIUS, vertex.x + ARC_RADIUS,
                vertex.y + ARC_RADIUS);
        }

        invalidate();
    }
}
