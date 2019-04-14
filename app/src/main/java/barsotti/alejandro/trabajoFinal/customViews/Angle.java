package barsotti.alejandro.trabajoFinal.customViews;

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

import java.util.ArrayList;
import java.util.Locale;

import barsotti.alejandro.trabajoFinal.utils.MathUtils;
import barsotti.alejandro.trabajoFinal.customInterfaces.IOnCartesianAxesPointChangeListener;

import static barsotti.alejandro.trabajoFinal.utils.MathUtils.calculateSweepAngleFromThreePoints;
import static barsotti.alejandro.trabajoFinal.utils.MathUtils.distanceBetweenPoints;
import static barsotti.alejandro.trabajoFinal.utils.MathUtils.extendEndPointToDistance;

public class Angle extends Shape implements IOnCartesianAxesPointChangeListener {
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

    /**
     * Color utilizado para la representación gráfica del ángulo.
     */
    private int shapeColor = Color.GREEN;

    private PointF OriginalAnglePoint;
    private PointF CartesianAxesOrigin;
    private PointF XCartesianAxisFirstPoint;
    private PointF XCartesianAxisSecondPoint;
    private PointF YCartesianAxisFirstPoint;
    private PointF YCartesianAxisSecondPoint;
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

    //region Getters
    public float getSweepAngleMeasure() {
        return mSweepAngle;
    }
    @Override
    protected int getNumberOfPointsInShape() {
        return NUMBER_OF_POINTS;
    }

    @Override
    protected int getShapeColor() {
        return shapeColor;
    }
    //endregion

    //region Setters
    public void setShapeColor(int color) {
        shapeColor = color;
        setupShapePaints();
        setupTextPaint();
    }
    //endregion

    //region Utils
    /**
     * Calcula las coordenadas en las cuales debe realizarse la representación gráfica de la medida del
     * ángulo de un objeto Angle de forma tal de que la misma no interfiera ni se solape con los segmentos
     * de la figura en cuestión.
     * @param vertex Vértice del ángulo.
     * @param firstEnd Primer extremo del ángulo.
     * @param secondEnd Segundo extremo del ángulo.
     * @return Punto que almacena las coordenadas en las cuales debe representarse la medida del ángulo.
     */
    private static PointF getCoordinatesForTextDrawing(PointF vertex, PointF firstEnd, PointF secondEnd) {
        // Calcular el vector entre el punto de inicio y el de fin.
        PointF firstVector = new PointF(firstEnd.x - vertex.x, firstEnd.y - vertex.y);
        float firstVectorLength = distanceBetweenPoints(firstEnd.x, firstEnd.y, vertex.x, vertex.y);
        PointF secondVector = new PointF(secondEnd.x - vertex.x, secondEnd.y - vertex.y);
        float secondVectorLength = distanceBetweenPoints(secondEnd.x, secondEnd.y,
            vertex.x, vertex.y);

        // Normalizar el vector.
        firstVector.x /= firstVectorLength;
        firstVector.y /= firstVectorLength;
        secondVector.x /= secondVectorLength;
        secondVector.y /= secondVectorLength;


        PointF pointInBisection = new PointF(firstVector.x + secondVector.x + vertex.x,
            firstVector.y + secondVector.y + vertex.y);

        PointF coordinates = extendEndPointToDistance(vertex, pointInBisection, -100,
            false);
        coordinates.x -= 75;

        return coordinates;
    }

    /**
     * Calcula el ángulo de inicio necesario para la representación gráfica de un arco dentro de ángulo
     * formado por los tres puntos proporcionados.
     * @param points ArrayList de los tres puntos que forman el ángulo.
     * @return Magnitud del ángulo de inicio requerido para la representación gráfica del arco.
     */
    private static float calculateStartAngleFromThreePoints(ArrayList<PointF> points) {
        // Obtener extremos y vértice.
        PointF firstEnd = points.get(0);
        PointF vertex = points.get(1);
        PointF secondEnd = points.get(2);

        /*
        Establecer punto de inicio de referencia. El mismo se encontrará siempre una unidad a la derecha del
        vértice, y a la misma altura y. Esto resultará útil para calcular el startAngle desde el origen
        considerado por Android (eje X positivo) hasta cada extremo.
         */
        PointF startPoint = new PointF(vertex.x + 1, vertex.y);

        // Establecer los tres puntos que forman cada ángulo.
        ArrayList<PointF> firstStartAnglePoints = new ArrayList<>();
        firstStartAnglePoints.add(startPoint);
        firstStartAnglePoints.add(vertex);
        firstStartAnglePoints.add(firstEnd);

        ArrayList<PointF> secondStartAnglePoints = new ArrayList<>();
        secondStartAnglePoints.add(startPoint);
        secondStartAnglePoints.add(vertex);
        secondStartAnglePoints.add(secondEnd);

        /*
        Calcular cada ángulo. En caso de encontrarse por encima del punto de inicio de referencia, el valor
        a considerar para ese ángulo será la resta entre 360º y el valor obtenido. Esto se debe a que
        Android dibuja los ángulos desde el punto de inicio de referencia y en sentido horario).
         */
        float firstStartAngle = calculateSweepAngleFromThreePoints(firstStartAnglePoints);
        firstStartAngle = (firstEnd.y < startPoint.y ? 360 - firstStartAngle : firstStartAngle);
        float secondStartAngle = calculateSweepAngleFromThreePoints(secondStartAnglePoints);
        secondStartAngle = (secondEnd.y < startPoint.y ? 360 - secondStartAngle : secondStartAngle);

        // Calcular diferencia entre los ángulos.
        float diff = Math.max(firstStartAngle, secondStartAngle)
            - Math.min(firstStartAngle, secondStartAngle);

        /*
        Si la diferencia es menor a 180º, entonces el ángulo de inicio es el ángulo que culmina en el primer
        extremo. En caso contrario, será el ángulo que culmina en el segundo extremo.
         */
        return diff < 180 ? Math.min(firstStartAngle, secondStartAngle)
            : Math.max(firstStartAngle, secondStartAngle);
    }
    //endregion

    @Override
    protected void initializeShape() {
        // Establecer marca que determina que los parámetros de la figura se calculen constantemente.
        mComputeShapeConstantly = true;
        setupTextPaint();
    }

    private void setupTextPaint() {
        // Establecer parámetros de la pintura a utilizar para la representación gráfica de la medida del
        // ángulo.
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setColor(mShapePaint.getColor());
        mTextPaint.setTypeface(Typeface.SANS_SERIF);
        mTextPaint.setShadowLayer(5, 0, 0, Color.BLACK);
        mTextPaint.setTextSize(48);
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
        if (mShapePoints.size() > 0) {
            if (mShapePoints.size() == 1) {
                mShapePoints.clear();

                float distance = Math.max(mPointRadius * DEFAULT_SEGMENT_LENGTH_MULTIPLIER / mCurrentZoom,
                    SEGMENT_MIN_LENGTH);

                // Calcular distancia a cada punto que define los Ejes Cartesianos. Inicializar ángulo
                // en la orientación indicada.
                float distanceToFirstPointX = MathUtils.distanceBetweenPoints(OriginalAnglePoint.x,
                    OriginalAnglePoint.y, XCartesianAxisFirstPoint.x, XCartesianAxisFirstPoint.y);
                float distanceToSecondPointX = MathUtils.distanceBetweenPoints(OriginalAnglePoint.x,
                    OriginalAnglePoint.y, XCartesianAxisSecondPoint.x, XCartesianAxisSecondPoint.y);
                float distanceToFirstPointY = MathUtils.distanceBetweenPoints(OriginalAnglePoint.x,
                    OriginalAnglePoint.y, YCartesianAxisFirstPoint.x, YCartesianAxisFirstPoint.y);
                float distanceToSecondPointY = MathUtils.distanceBetweenPoints(OriginalAnglePoint.x,
                    OriginalAnglePoint.y, YCartesianAxisSecondPoint.x, YCartesianAxisSecondPoint.y);

                if (distanceToFirstPointX < distanceToSecondPointX) {
                    mShapePoints.add(MathUtils.extendEndPointToDistance(CartesianAxesOrigin,
                        XCartesianAxisFirstPoint, distance, false));
                }
                else {
                    mShapePoints.add(MathUtils.extendEndPointToDistance(CartesianAxesOrigin,
                        XCartesianAxisSecondPoint, distance, false));
                }

                if (distanceToFirstPointY < distanceToSecondPointY) {
                    mShapePoints.add(MathUtils.extendEndPointToDistance(CartesianAxesOrigin,
                        YCartesianAxisFirstPoint, distance, false));
                }
                else {
                    mShapePoints.add(MathUtils.extendEndPointToDistance(CartesianAxesOrigin,
                        YCartesianAxisSecondPoint, distance, false));
                }

                mShapePoints.add(1, CartesianAxesOrigin);
            }

            mSweepAngle = calculateSweepAngleFromThreePoints(mShapePoints);
            mStartAngle = calculateStartAngleFromThreePoints(mShapePoints);
        }
    }

    @Override
    public boolean addPoint(PointF point) {
        // Validar que sea posible agregar un nuevo punto a la figura.
        if (mShapePoints.size() < getNumberOfPointsInShape()) {
            mShapePoints.add(point);
            OriginalAnglePoint = point;

            computeShape();

            // Requerir redibujo para mostrar el nuevo punto.
            invalidate();
        }

        return mShapePoints.size() < getNumberOfPointsInShape();
    }

    @Override
    public void updateViewMatrix(Matrix matrix) {
        super.updateViewMatrix(matrix);

        // Mapear las variables propias del ángulo según la nueva matriz.
        mMappedShapePoints = mapPoints(mCurrentMatrix, mShapePoints);

        if (mMappedShapePoints.size() == NUMBER_OF_POINTS) {
            mVertex = mMappedShapePoints.get(1);
            mFirstEnd = extendEndPointToDistance(mVertex, mMappedShapePoints.get(0),
                SEGMENT_MIN_LENGTH, true);
            mSecondEnd = extendEndPointToDistance(mVertex, mMappedShapePoints.get(2),
                SEGMENT_MIN_LENGTH, true);
            mTextCoordinates = getCoordinatesForTextDrawing(mVertex, mFirstEnd, mSecondEnd);

            mArcOval.set(mVertex.x - ARC_RADIUS, mVertex.y - ARC_RADIUS,
                mVertex.x + ARC_RADIUS, mVertex.y + ARC_RADIUS);
        }

        invalidate();
    }

    @Override
    public void updateCartesianAxesPoints(float[] cartesianAxesPoints, PointF cartesianAxesOrigin) {
        mShapePoints.clear();
        if (OriginalAnglePoint != null) {
            mShapePoints.add(OriginalAnglePoint);
        }

        XCartesianAxisFirstPoint = new PointF(cartesianAxesPoints[0], cartesianAxesPoints[1]);
        XCartesianAxisSecondPoint = new PointF(cartesianAxesPoints[2], cartesianAxesPoints[3]);
        YCartesianAxisFirstPoint = new PointF(cartesianAxesPoints[4], cartesianAxesPoints[5]);
        YCartesianAxisSecondPoint = new PointF(cartesianAxesPoints[6], cartesianAxesPoints[7]);
        CartesianAxesOrigin = cartesianAxesOrigin;

        // Calcular la medida del ángulo representado y el ángulo de inicio.
        computeShape();

        // Actualizar valores necesarios para la representación gráfica.
        updateViewMatrix(null);
    }
}
