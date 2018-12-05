package barsotti.alejandro.prototipotf.customViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import barsotti.alejandro.prototipotf.Utils.MathUtils;

public class Angle extends Shape {
    // Tag utilizado con fines de debug.
    private final String TAG = "Angle";
    // Número que indica la cantidad de puntos que componen el ángulo. El orden en que se encuentran es:
    // primer extremo, vértice, segundo extremo.
    private static final int NUMBER_OF_POINTS = 3;
    /**
     * Medida del ángulo actual representado por la figura.
     */
    private float mAngle;

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
            // Dibujar borde.
            canvas.drawLine(mMappedShapePoints.get(0).x, mMappedShapePoints.get(0).y, // Primer extremo.
                mMappedShapePoints.get(1).x, mMappedShapePoints.get(1).y, // Vértice.
                mIsSelected ? mSelectedShapeBorderPaint : mShapeBorderPaint);
            canvas.drawLine(mMappedShapePoints.get(1).x, mMappedShapePoints.get(1).y, // Vértice.
                mMappedShapePoints.get(2).x, mMappedShapePoints.get(2).y, // Segundo extremo.
                mIsSelected ? mSelectedShapeBorderPaint : mShapeBorderPaint);

            // Dibujar línea principal.
            canvas.drawLine(mMappedShapePoints.get(0).x, mMappedShapePoints.get(0).y, // Primer extremo.
                mMappedShapePoints.get(1).x, mMappedShapePoints.get(1).y, // Vértice.
                mIsSelected ? mSelectedShapePaint : mShapePaint);
            canvas.drawLine(mMappedShapePoints.get(1).x, mMappedShapePoints.get(1).y, // Vértice.
                mMappedShapePoints.get(2).x, mMappedShapePoints.get(2).y, // Segundo extremo.
                mIsSelected ? mSelectedShapePaint : mShapePaint);

            // Dibujar texto que indica la medida del Ángulo.
            Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mTextPaint.setStyle(Paint.Style.STROKE);
//            mTextPaint.setLetterSpacing(2);
            mTextPaint.setColor(mShapePaint.getColor());
            mTextPaint.setTypeface(Typeface.SANS_SERIF);
            mTextPaint.setTextSize(24);

            canvas.drawText(String.valueOf(mAngle), mMappedShapePoints.get(1).x + 25,
                mMappedShapePoints.get(1).y + 25, mTextPaint);
        }

        super.onDraw(canvas);
    }

    @Override
    public boolean checkTouchToSelect(PointF point) {
        // TODO: completar.
        return false;
    }

    @Override
    protected void computeShape() {
        if (mShapePoints.size() == NUMBER_OF_POINTS) {
            mAngle = MathUtils.angleFromThreePoints(mShapePoints);
        }
    }

    @Override
    public void updateViewMatrix(Matrix matrix) {
        super.updateViewMatrix(matrix);

        // Mapear las variables propias del ángulo según la nueva matriz.
        mMappedShapePoints = mapPoints(mCurrentMatrix, mShapePoints);

        invalidate();
    }
}
