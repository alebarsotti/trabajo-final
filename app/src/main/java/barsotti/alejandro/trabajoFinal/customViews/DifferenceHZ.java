package barsotti.alejandro.trabajoFinal.customViews;

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

import java.util.Locale;

import barsotti.alejandro.trabajoFinal.customInterfaces.IOnCircumferenceCenterChangeListener;
import barsotti.alejandro.trabajoFinal.customInterfaces.IOnToothPitchChangeListener;
import barsotti.alejandro.trabajoFinal.utils.MathUtils;

public class DifferenceHZ extends Shape implements IOnCircumferenceCenterChangeListener,
    IOnToothPitchChangeListener {
    //region Constantes
    /**
     * Tag utilizado con fines de debug.
     */
    private static final String TAG = "DifferenceHZ";
    /**
     * Número que indica la cantidad de puntos que componen la forma.
     */
    private static final int NUMBER_OF_POINTS = 1;
    /**
     * Color utilizado para la representación gráfica de la forma.
     */
    private static final int SHAPE_COLOR = Color.YELLOW;
    //endregion

    //region Propiedades
    /**
     * Punto que indica las coordenadas del centro de la circunferencia de la cual depende la tangente.
     */
    private PointF circumferenceCenter;
    /**
     * Número que indica la medida del radio de la circunferencia de la cual depende la tangente.
     */
    private float circumferenceRadius;
    private float differenceHzValueInMillimeters;
    private float millimetersPerPixel;
    private PointF pointInCircumference;
    private PointF mappedPointInCircumference;
    private Paint textPaint = new Paint();
    /**
     * Cultura utilizada para la representación gráfica de la medida de h. Permite que se muestre el
     * separador de decimales correcto para la región (coma).
     */
    private Locale locale = new Locale("es", "ES");
    //endregion

    //region Constructores
    public DifferenceHZ(Context context) {
        this(context, null);
    }

    public DifferenceHZ(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initializeShape();
        Log.d(TAG, "Constructor.");
    }
    //endregion

    @Override
    public void updateCircumferenceCenterAndRadius(PointF center, float radius) {
        circumferenceCenter = center;
        circumferenceRadius = radius;

        computeShape();
    }

    @Override
    protected void initializeShape() {
        // Establecer parámetros de la pintura a utilizar para la representación gráfica de h.
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setColor(mShapePaint.getColor());
        textPaint.setTypeface(Typeface.SANS_SERIF);
        textPaint.setShadowLayer(5, 0, 0, Color.BLACK);
        textPaint.setTextSize(48);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMappedShapePoints.size() == NUMBER_OF_POINTS) {
            // Dibujar borde de la forma.
            canvas.drawLine(mMappedShapePoints.get(0).x, mMappedShapePoints.get(0).y,
                mappedPointInCircumference.x, mappedPointInCircumference.y,
                mIsSelected ? mSelectedShapeBorderPaint : mShapeBorderPaint);

            // Dibujar línea principal de la forma.
            canvas.drawLine(mMappedShapePoints.get(0).x, mMappedShapePoints.get(0).y,
                mappedPointInCircumference.x, mappedPointInCircumference.y,
                mIsSelected ? mSelectedShapePaint : mShapePaint);

            // Dibujar medida de h.
            canvas.drawText(
                differenceHzValueInMillimeters != 0 ?
                    String.format(locale, "%.2f mm", differenceHzValueInMillimeters) : "?",
                mMappedShapePoints.get(0).x,
                mMappedShapePoints.get(0).y - mPointRadius, textPaint);
        }

        super.onDraw(canvas);
    }

    @Override
    protected int getNumberOfPointsInShape() {
        return NUMBER_OF_POINTS;
    }

    @Override
    protected int getShapeColor() {
        return SHAPE_COLOR;
    }

    @Override
    public float computeDistanceBetweenTouchAndShape(PointF point) {
        if (mMappedShapePoints.size() != NUMBER_OF_POINTS) {
            return -1;
        }

        float distance = MathUtils.distanceBetweenSegmentAndPoint(point, mMappedShapePoints.get(0),
            mappedPointInCircumference);

        return distance <= TOUCH_TOLERANCE ? distance : -1;
    }

    @Override
    protected void computeShape() {
        if (mShapePoints.size() == NUMBER_OF_POINTS) {
            PointF shapePoint = mShapePoints.get(0);
            float distanceBetweenPointAndCircumferenceCenter = MathUtils.distanceBetweenPoints(shapePoint.x,
                shapePoint.y, circumferenceCenter.x, circumferenceCenter.y);
            float differenceHzValueInPixels = distanceBetweenPointAndCircumferenceCenter - circumferenceRadius;
            pointInCircumference = MathUtils.translatePointToCircumference(circumferenceCenter,
                circumferenceRadius, shapePoint);
            differenceHzValueInMillimeters = Math.abs(differenceHzValueInPixels * millimetersPerPixel);
        }
    }

    @Override
    public void onToothPitchValueChange(float millimetersPerPixel) {
        this.millimetersPerPixel = millimetersPerPixel;

        computeShape();
    }

    @Override
    public void updateViewMatrix(Matrix matrix) {
        super.updateViewMatrix(matrix);

        mMappedShapePoints = mapPoints(mCurrentMatrix, mShapePoints);
        mappedPointInCircumference = mapPoint(mCurrentMatrix, pointInCircumference);

        invalidate();
    }
}
