package barsotti.alejandro.trabajoFinal.customViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;

import barsotti.alejandro.trabajoFinal.customInterfaces.ICircumference;
import barsotti.alejandro.trabajoFinal.customInterfaces.IOnCircumferenceCenterChangeListener;
import barsotti.alejandro.trabajoFinal.utils.MathUtils;

import static barsotti.alejandro.trabajoFinal.utils.MathUtils.TOLERANCE;

public class Circumference extends Shape implements ICircumference {
    //region Constantes
    /**
     * Tag utilizado con fines de debug.
     */
    private static final String TAG = "Circumference";
    /**
     * Número que indica la cantidad de puntos que componen la circunferencia.
     */
    private static final int NUMBER_OF_POINTS = 3;
    /**
     * Número que indica la cantidad de puntos a utilizar para representar gráficamente la circunferencia.
     */
    public static final int NUMBER_OF_POINTS_TO_DRAW = 360;
    /**
     * Color utilizado para la representación gráfica de la circunferencia.
     */
    private static final int SHAPE_COLOR = Color.RED;
    /**
     * Número utilizado para los cálculos de curvas de Bezier.
     */
    public static final float BEZIER_CURVE_CONSTANT = 0.551915024494f;
    //endregion

    //region Propiedades
    /**
     * Punto que indica las coordenadas del centro de la circunferencia.
     */
    private PointF mCenter;
    /**
     * Número que indica la longitud del radio de la circunferencia.
     */
    private float mRadius;
    /**
     * Punto que indica las coordenadas del centro de la circunferencia mapeado según la matriz actual.
     */
    public PointF mMappedCenter;
    /**
     * Número que indica la longitud del radio de la circunferencia mapeado según la matriz actual.
     */
    public float mMappedRadius;
    /**
     * Lista de puntos (en formato [x1, y1, x2, y2, ...]) utilizados para representar gráficamente la
     * circunferencia.
     */
    private float[] mPathPoints = new float[NUMBER_OF_POINTS_TO_DRAW * 4];
    /**
     * Lista de puntos (en formato [x1, y1, x2, y2, ...]) utilizados para representar gráficamente la
     * circunferencia mapeados según la matriz actual.
     */
    private float[] mMappedPathPoints = new float[NUMBER_OF_POINTS_TO_DRAW * 4];
    /**
     * Lista de suscriptores a eventos de actualización de la circunferencia.
     */
    private ArrayList<IOnCircumferenceCenterChangeListener> mListeners = new ArrayList<>();
    //endregion

    //region Constructores
    public Circumference(Context context) {
        this(context, null);
    }

    public Circumference(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initializeShape();
        Log.d(TAG, "Constructor.");
    }
    //endregion

    //region Administración de listeners
    @Override
    public void addOnCircumferenceCenterChangeListener(IOnCircumferenceCenterChangeListener listener) {
        mListeners.add(listener);
        listener.updateCircumferenceCenterAndRadius(mCenter, mRadius);
    }

    @Override
    public void removeOnCircumferenceCenterChangeListener(IOnCircumferenceCenterChangeListener listener) {
        try {
            mListeners.remove(listener);
        }
        catch (Exception e) {
            Log.d(TAG, "removeOnCircumferenceCenterChangeListener: the object was not found in the " +
                "list.");
        }
    }
    //endregion

    @Override
    protected void initializeShape() {
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
        // Dibujar circunferencia si la misma se encuentra completa.
        if (mPathPoints != null) {
            // Dibujar borde.
            canvas.drawLines(mMappedPathPoints, mIsSelected ? mSelectedShapeBorderPaint : mShapeBorderPaint);
            // Dibujar línea principal.
            canvas.drawLines(mMappedPathPoints, mIsSelected ? mSelectedShapePaint : mShapePaint);
        }

        super.onDraw(canvas);
    }

    @Override
    protected void computeShape() {
        if (mShapePoints.size() == NUMBER_OF_POINTS) {
            // Calcular centro y radio de la circunferencia a partir de tres puntos dados.
            computeCenterAndRadius();

            // Establecer la lista de N puntos que conforman la circunferencia. N = NUMBER_OF_POINTS_TO_DRAW.
            computePathPoints();
        }

        // Informar a los suscriptores sobre la actualización de los valores de centro y radio.
        for (IOnCircumferenceCenterChangeListener listener: mListeners) {
            listener.updateCircumferenceCenterAndRadius(mCenter, mRadius);
        }
    }

    @Override
    public void updateViewMatrix(Matrix matrix) {
        super.updateViewMatrix(matrix);

        // Mapear las variables propias de la circunferencia según la nueva matriz.
        mMappedRadius = mCurrentMatrix.mapRadius(mRadius);
        mMappedCenter = mapPoint(mCurrentMatrix, mCenter);
        mMappedShapePoints = mapPoints(mCurrentMatrix, mShapePoints);
        mCurrentMatrix.mapPoints(mMappedPathPoints, mPathPoints);

        invalidate();
    }

    @Override
    public float computeDistanceBetweenTouchAndShape(PointF point) {
        // Si la circunferencia no se encuentra completa, no medir distancia.
        if (mMappedCenter == null) {
            return -1;
        }

        // Determinar la distancia entre el punto y el centro de la circunferencia.
        float distanceFromCenterToPoint = MathUtils.distanceBetweenPoints(mMappedCenter.x, mMappedCenter.y,
            point.x, point.y);

        /* La distancia del toque estará dada por el valor absoluto de la diferencia entre el radio de la
        circunferencia y la distancia medida entre el punto y el centro de la misma, dado que la
        distancia relevante es la existente entre su borde y el punto. */
        float distanceFromCircumferenceToPoint = Math.abs(mMappedRadius - distanceFromCenterToPoint);

        // Devolver la distancia medida si la misma se encuentra dentro de la tolerancia definida.
        return distanceFromCircumferenceToPoint <= TOUCH_TOLERANCE ? distanceFromCircumferenceToPoint : -1;
    }

    /**
     * Calcula y establece nuevos valores para el centro y el radio de la circunferencia.
     */
    public void computeCenterAndRadius() {
        float point1X, point1Y, point2X, point2Y, point3X, point3Y, delta1, delta2, point12X, point12Y,
            point23X, point23Y, x, y;

        // Separar coordenadas de los puntos de la circunferencia con el fin de utilizarlos fácilmente.
        point1X = mShapePoints.get(0).x;
        point1Y = mShapePoints.get(0).y;
        point2X = mShapePoints.get(1).x;
        point2Y = mShapePoints.get(1).y;
        point3X = mShapePoints.get(2).x;
        point3Y = mShapePoints.get(2).y;

        // Calcular deltas entre puntos.
        delta1 = (point2X - point1X) / (point2Y - point1Y);
        delta2 = (point3X - point2X) / (point3Y - point2Y);

        // Controlar delta2 - delta1 != 0. De lo contrario, no se podría calcular la circunferencia.
        if (Math.abs(delta2 - delta1) < TOLERANCE) {
            // El cálculo no se puede realizar. Devolver circunferencia por defecto.
            mCenter = new PointF(0, 0);
            mRadius = 0;

            return;
        }

        /*
        Calcular punto intermedio entre los puntos 1 y 2, y entre los puntos 2 y 3. Estos puntos indican la
        ubicación de los bisectores perpendiculares de las líneas que unen cada par de puntos.
         */
        point12X = (point1X + point2X) / 2;
        point12Y = (point1Y + point2Y) / 2;
        point23X = (point2X + point3X) / 2;
        point23Y = (point2Y + point3Y) / 2;

        // Calcular coordenadas del centro de la circunferencia.
        x = (point23Y + point23X * delta2 - point12Y - point12X * delta1) / (delta2 - delta1);
        y = -1 * x * delta1 + point12Y + point12X * delta1;
        mCenter = new PointF(x, y);

        // Calcular radio.
        mRadius = (float) Math.sqrt(Math.pow(x - point1X, 2) + Math.pow(y - point1Y, 2));
    }

    /**
     * Establece la nueva lista de puntos que representan a la circunferencia.
     */
    public void computePathPoints() {
        // Crear path que representa la circunferencia mediante curvas Bézier.
        float deltaM = BEZIER_CURVE_CONSTANT * mRadius;
        float x = mCenter.x;
        float y = mCenter.y;
        Path newPath = new Path();
        newPath.moveTo(x, y + mRadius);
        newPath.cubicTo(x + deltaM, y + mRadius, x + mRadius, y + deltaM, x + mRadius, y);
        newPath.cubicTo(x + mRadius, y - deltaM, x + deltaM, y - mRadius, x, y - mRadius);
        newPath.cubicTo(x - deltaM, y - mRadius, x - mRadius, y - deltaM, x - mRadius, y);
        newPath.cubicTo(x - mRadius, y + deltaM, x - deltaM, y + mRadius, x, y + mRadius);
        newPath.close();

        // Calcular coordenadas de los puntos que forman la circunferencia.
        int numberOfPoints = Circumference.NUMBER_OF_POINTS_TO_DRAW;
        PathMeasure pathMeasure = new PathMeasure(newPath, false);
        float distance = 0f;
        float deltaDistance = pathMeasure.getLength() / numberOfPoints;
        int index = 0;
        float[] coordinatesComputedForPoint = new float[2];
        float[] pointCoordinates = new float[numberOfPoints * 4];

        for (int counter = 0; counter < numberOfPoints; counter++) {
            pathMeasure.getPosTan(distance, coordinatesComputedForPoint, null);
            pointCoordinates[index++] = coordinatesComputedForPoint[0];
            pointCoordinates[index++] = coordinatesComputedForPoint[1];
            if (counter != 0) {
                pointCoordinates[index++] = coordinatesComputedForPoint[0];
                pointCoordinates[index++] = coordinatesComputedForPoint[1];
            }
            else {
                pointCoordinates[pointCoordinates.length - 2] = coordinatesComputedForPoint[0];
                pointCoordinates[pointCoordinates.length - 1] = coordinatesComputedForPoint[1];
            }

            distance = distance + deltaDistance;
        }
        
        mPathPoints = pointCoordinates;
    }
}