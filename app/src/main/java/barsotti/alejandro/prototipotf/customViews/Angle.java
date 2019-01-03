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

import barsotti.alejandro.prototipotf.utils.MathUtils;
import barsotti.alejandro.prototipotf.customInterfaces.IOnTangentPointChangeListener;

public class Angle extends Shape implements IOnTangentPointChangeListener {
    //region Constantes
    /**
     * Tag utilizado con fines de debug.
     */
    private static final String TAG = "Angle";
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
    private static final int SEGMENT_MIN_LENGTH = ARC_RADIUS * 3;
    /**
     * Número utilizado para calcular la longitud inicial de los segmentos del ángulo.
     */
    private static final int DEFAULT_SEGMENT_LENGTH_MULTIPLIER = 3;
    /**
     * Color utilizado para la representación gráfica del ángulo.
     */
    private static final int SHAPE_COLOR = Color.GREEN;
    //endregion

    //region Propiedades
    /**
     * Medida del ángulo actual representado por la figura.
     */
    private float mSweepAngle;
    /**
     * Medida del ángulo de inicio de la figura (medido desde el eje X positivo hasta el primer segmento del
     * ángulo en sentido horario. Utilizado para la representación gráfica, dadas las especificaciones de
     * Android.
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
    /**
     * Punto que representa el extremo final del primer segmento que compone el ángulo. Utilizado para la
     * representación gráfica.
     */
    private PointF mFirstEnd = new PointF();
    /**
     * Punto que representa el extremo final del segundo segmento que compone el ángulo. Utilizado para la
     * representación gráfica.
     */
    private PointF mSecondEnd = new PointF();
    /**
     * Punto que representa el vértice del ángulo. Utilizado para la representación gráfica.
     */
    private PointF mVertex = new PointF();
    /**
     * Cultura utilizada para la representación gráfica de la medida del ángulo. Permite, en particular, que
     * se muestre el separador de decimales correcto para la región (coma).
     */
    private Locale mLocale = new Locale("es", "ES");
    /**
     * Punto que almacena las coordenadas utilizadas para la representación gráfica de la medida del ángulo.
     */
    private PointF mTextCoordinates;
    //endregion

    //region Constructores
    public Angle(Context context) {
        this(context, null);
    }

    public Angle(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initializeShape();
        Log.d(TAG, "Constructor.");
    }
    //endregion

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
        return SHAPE_COLOR;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Dibujar ángulo solo si sus puntos están definidos.
        if (mMappedShapePoints.size() == NUMBER_OF_POINTS) {
            // Dibujar borde del arco.
            canvas.drawArc(mArcOval, mStartAngle, mSweepAngle, false,
                mIsSelected ? mSelectedShapeBorderPaint : mShapeBorderPaint);

            // Dibujar bordes del ángulo.
            canvas.drawLine(mFirstEnd.x, mFirstEnd.y, // Primer extremo.
                mVertex.x, mVertex.y, // Vértice.
                mIsSelected ? mSelectedShapeBorderPaint : mShapeBorderPaint);
            canvas.drawLine(mVertex.x, mVertex.y, // Vértice.
                mSecondEnd.x, mSecondEnd.y, // Segundo extremo.
                mIsSelected ? mSelectedShapeBorderPaint : mShapeBorderPaint);

            // Dibujar línea principal del arco.
            canvas.drawArc(mArcOval, mStartAngle, mSweepAngle, false,
                mIsSelected ? mSelectedShapePaint : mShapePaint);

            // Dibujar líneas principales del ángulo.
            canvas.drawLine(mFirstEnd.x, mFirstEnd.y, // Primer extremo.
                mVertex.x, mVertex.y, // Vértice.
                mIsSelected ? mSelectedShapePaint : mShapePaint);
            canvas.drawLine(mVertex.x, mVertex.y, // Vértice.
                mSecondEnd.x, mSecondEnd.y, // Segundo extremo.
                mIsSelected ? mSelectedShapePaint : mShapePaint);

            // Dibujar texto que indica la medida del Ángulo.
            canvas.drawText(String.format(mLocale, "%.2fº", mSweepAngle), mTextCoordinates.x,
                mTextCoordinates.y, mTextPaint);
        }

        super.onDraw(canvas);
    }

    @Override
    public float computeDistanceBetweenTouchAndShape(PointF point) {
        // Si la figura no se encuentra completa, no calcular distancia.
        if (mMappedShapePoints.size() != NUMBER_OF_POINTS) {
            return -1;
        }

        // Calcular distancia entre el punto y cada segmento que compone el ángulo. Elegir el menor valor.
        float distanceFromSegment1ToPoint = MathUtils.distanceBetweenSegmentAndPoint(point,
            mFirstEnd, mVertex);
        float distanceFromSegment2ToPoint = MathUtils.distanceBetweenSegmentAndPoint(point,
            mVertex, mSecondEnd);
        float minDistance = Math.min(distanceFromSegment1ToPoint, distanceFromSegment2ToPoint);

        // Devolver un valor solo si la mínima distancia se encuentra dentro de la tolerancia definida.
        return minDistance <= TOUCH_TOLERANCE ? minDistance : -1;
    }

    @Override
    protected void computeShape() {
        // Si la figura se encuentra completa, calcular ángulo representado y ángulo de inicio.
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
            mFirstEnd = MathUtils.extendEndPointToDistance(mVertex, mMappedShapePoints.get(0),
                SEGMENT_MIN_LENGTH, true);
            mSecondEnd = MathUtils.extendEndPointToDistance(mVertex, mMappedShapePoints.get(2),
                SEGMENT_MIN_LENGTH, true);
            mTextCoordinates = MathUtils.getCoordinatesForTextDrawing(mVertex, mFirstEnd, mSecondEnd);

            mArcOval.set(mVertex.x - ARC_RADIUS, mVertex.y - ARC_RADIUS,
                mVertex.x + ARC_RADIUS, mVertex.y + ARC_RADIUS);
        }

        invalidate();
    }

    @Override
    public void updateTangentPoints(PointF tangentPoint, PointF linePoint, PointF circleCenterPoint) {
        // Cuando la posición del punto de la tangente cambie, calcular nuevamente el ángulo por defecto en
        // la nueva ubicación.
        mShapePoints.clear();

        // Establecer primer extremo del ángulo (ubicado sobre la línea tangente).
        mShapePoints.add(MathUtils.extendEndPointToDistance(tangentPoint, linePoint,
            Math.max(mPointRadius * DEFAULT_SEGMENT_LENGTH_MULTIPLIER / mCurrentZoom, SEGMENT_MIN_LENGTH),
            false));

        // Establecer vértice del ángulo (punto tangente).
        mShapePoints.add(new PointF(tangentPoint.x, tangentPoint.y));

        // Establecer segundo extremo del ángulo (ubicado sobre la recta radial de la tangente).
        mShapePoints.add(MathUtils.extendEndPointToDistance(tangentPoint, circleCenterPoint,
            Math.max(mPointRadius * DEFAULT_SEGMENT_LENGTH_MULTIPLIER / mCurrentZoom, SEGMENT_MIN_LENGTH),
            false));

        // Calcular la medida del ángulo representado y el ángulo de inicio.
        computeShape();

        // Actualizar valores necesarios para la representación gráfica.
        updateViewMatrix(null);
    }
}
