package barsotti.alejandro.prototipotf.customViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;

import barsotti.alejandro.prototipotf.utils.MathUtils;
import barsotti.alejandro.prototipotf.customInterfaces.IOnCircumferenceCenterChangeListener;
import barsotti.alejandro.prototipotf.customInterfaces.IOnTangentPointChangeListener;
import barsotti.alejandro.prototipotf.customInterfaces.ITangent;

import static barsotti.alejandro.prototipotf.utils.MathUtils.MAX_NUMBER_OF_POINTS_PER_SEGMENT;
import static barsotti.alejandro.prototipotf.utils.MathUtils.TOLERANCE;


public class Tangent extends Shape implements IOnCircumferenceCenterChangeListener, ITangent {
// TODO: La recta radial debería tener un inicio con linea dibujada (la mitad de un segmento, mínimamente).

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
    private float[] mPathPoints = new float[MAX_NUMBER_OF_POINTS_PER_SEGMENT * 2];
    /**
     * Lista de puntos utilizados para representar la recta radial que acompaña a la tangente mapeados según
     * la matriz actual.
     */
    private float[] mMappedPathPoints = new float[MAX_NUMBER_OF_POINTS_PER_SEGMENT * 2];
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
            PointF pointInCircumference = translatePointToCircumference(mCircumferenceCenter,
                mCircumferenceRadius, mShapePoints.get(0));
            mShapePoints.set(0, pointInCircumference);
            // Calcular dos puntos que definen la tangente.
            mTangentPoints = tangentToCircumference(mCircumferenceCenter, pointInCircumference,
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

    /**
     * A partir de datos de una circunferencia y un punto 'P', calcula el punto 'Q' más cercano a P que se
     * encuentra sobre la circunferencia.
     * @param center Centro de la circunferencia.
     * @param radius Radio de la circunferencia.
     * @param point Punto a trasladar.
     * @return Punto más cercano a P que se encuentra sobre la circunferencia indicada.
     */
    public static PointF translatePointToCircumference(PointF center, double radius, PointF point) {
        // Calcular coordenada X del punto de la circunferencia a encontrar.
        double tx = (radius * (point.x - center.x)) /
            Math.sqrt(Math.pow(point.x - center.x, 2) + Math.pow(point.y - center.y, 2))
            + center.x;

        // Calcular coordenada Y del punto de la circunferencia a encontrar.
        double ty = (radius * (point.y - center.y)) /
            Math.sqrt(Math.pow(point.x - center.x, 2) + Math.pow(point.y - center.y, 2))
            + center.y;

        return new PointF((float) tx, (float) ty);
    }

    /**
     * Calcula la tangente a una circunferencia en un punto dado.
     * @param center Punto que representa el centro de la circunferencia.
     * @param pointInCircumference Punto perteneciente a la circunferencia a partir del cual se calculará
     *                             la tangente.
     * @param circumferenceRadius Radio de la circunferencia.
     * @return Array con dos puntos ([x1, y1, x2, y2]) que, al unirse mediante una recta, representan la
     * tangente calculada.
     */
    public static float[] tangentToCircumference(PointF center, PointF pointInCircumference,
                                                 float circumferenceRadius) {
        ArrayList<PointF> tangentPointsList = new ArrayList<>();

        // Determinar si la diferencia en la coordenada "y" de los puntos es cero (caso especial).
        double tangentPointDistance = 2 * circumferenceRadius;
        if (Math.abs(pointInCircumference.y - center.y) < TOLERANCE) {
            tangentPointsList.add(new PointF(pointInCircumference.x,
                (float) (pointInCircumference.y + tangentPointDistance)));
            tangentPointsList.add(new PointF(pointInCircumference.x,
                (float) (pointInCircumference.y - tangentPointDistance)));
        }
        else {
            // Los nombres de variables "a" y "b" hacen referencia a la fórmula de la recta: y = a * x + b.
            double a = -1 * (pointInCircumference.x - center.x) / (pointInCircumference.y - center.y);
            double b = pointInCircumference.y - pointInCircumference.x * a;

            double firstPointX = pointInCircumference.x - tangentPointDistance;
            double firstPointY = a * firstPointX + b;
            double secondPointX = pointInCircumference.x + tangentPointDistance;
            double secondPointY = a * secondPointX + b;

            tangentPointsList.add(new PointF((float) firstPointX, (float) firstPointY));
            tangentPointsList.add(new PointF((float) secondPointX, (float) secondPointY));
        }

        float[] tangentPointsArray = new float[4];
        int index = 0;
        for (PointF point: tangentPointsList) {
            tangentPointsArray[index++] = point.x;
            tangentPointsArray[index++] = point.y;
        }

        return tangentPointsArray;
    }
}