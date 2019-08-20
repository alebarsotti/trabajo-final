package barsotti.alejandro.tf.utils;

import android.graphics.PointF;
import java.util.ArrayList;

public class MathUtils {

    /**
     * Valor de tolerancia utilizado para la realización de cálculos precisos.
     **/
    public static final double TOLERANCE = 0.0001d;

    /**
     * Calcula la distancia entre dos puntos dados.
     * @param x1 Coordenada X del primer punto.
     * @param y1 Coordenada Y del primer punto.
     * @param x2 Coordenada X del segundo punto.
     * @param y2 Coordenada Y del segundo punto.
     * @return Medida de la distancia entre los puntos especificados.
     */
    public static float distanceBetweenPoints(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    /**
     * Calcula la distancia entre un punto fijo y la recta definida por otros dos puntos.
     * Referencia de fórmula: https://brilliant.org/wiki/dot-product-distance-between-point-and-a-line
     * @param point Punto fijo a partir del cual calcular la distancia.
     * @param linePoint1 Punto que forma parte de la recta.
     * @param linePoint2 Punto que forma parte de la recta.
     * @return Distancia entre el punto y la recta proporcionados.
     */
    public static float distanceBetweenLineAndPoint(PointF point, PointF linePoint1, PointF linePoint2) {
        float a, b = -1, c;
        a = (linePoint1.y - linePoint2.y) / (linePoint1.x - linePoint2.x);
        c = (-1 * a * linePoint1.x) + linePoint1.y;

        return (float) (Math.abs(a * point.x + b * point.y + c) /
            Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2)));
    }

    /**
     * Calcula un ángulo a partir de una lista de tres puntos (primer extremo, vértice, segundo extremo),
     * utilizando la Ley de los Cosenos.
     * Referencia de fórmula y cálculos: https://www.mathsisfun.com/algebra/trig-cosine-law.html
     * @param points Lista de puntos a partir de los cuales calcular el ángulo.
     * @return Medida del ángulo calculado.
     */
    public static float calculateSweepAngleFromThreePoints(ArrayList<PointF> points) {
        // Obtener extremos y vértice.
        PointF firstEnd = points.get(0);
        PointF vertex = points.get(1);
        PointF secondEnd = points.get(2);

        // Calcular distancia entre cada par de puntos.
        double firstEndVertexDistance = distanceBetweenPoints(firstEnd.x, firstEnd.y,
            vertex.x, vertex.y);
        double secondEndVertexDistance = distanceBetweenPoints(secondEnd.x, secondEnd.y,
            vertex.x, vertex.y);
        double firstEndSecondEndDistance = distanceBetweenPoints(firstEnd.x, firstEnd.y,
            secondEnd.x, secondEnd.y);

        // Aplicar Ley de los Cosenos.
        double angle = Math.toDegrees(Math.acos((Math.pow(secondEndVertexDistance, 2)
            + Math.pow(firstEndVertexDistance, 2) - Math.pow(firstEndSecondEndDistance, 2))
            / 2 / secondEndVertexDistance / firstEndVertexDistance)
        );

        return (float) angle;
    }

    /**
     * Calcula la distancia entre un punto fijo y el segmento definido por otros dos puntos.
     * Procedimiento:
     *  1- Calcular ángulo formado por el punto fijo, el punto 1 y el punto 2.
     *    Realizar el mismo cálculo para el ángulo formado por el punto fijo, el punto 2 y el punto 1.
     *  2- Si alguno de esos ángulos es obtuso, significa que la distancia a calcular es aquella existente
     *    entre el punto fijo y el vértice de ese ángulo.
     *  3- En caso contrario, la distancia a calcular será aquella existente entre el punto fijo y la recta
     *    formada por los extremos del segmento.
     * @param point Punto fijo a partir del cual calcular la distancia.
     * @param segmentPoint1 Primer extremo del segmento.
     * @param segmentPoint2 Segundo extremo del segmento.
     * @return Distancia entre el punto y el segmento proporcionados.
     */
    public static float distanceBetweenSegmentAndPoint(PointF point, PointF segmentPoint1,
                                                        PointF segmentPoint2) {
        // Establecer los tres puntos que forman cada ángulo.
        ArrayList<PointF> firstAnglePoints = new ArrayList<>();
        firstAnglePoints.add(point);
        firstAnglePoints.add(segmentPoint1);
        firstAnglePoints.add(segmentPoint2);

        ArrayList<PointF> secondAnglePoints = new ArrayList<>();
        secondAnglePoints.add(point);
        secondAnglePoints.add(segmentPoint2);
        secondAnglePoints.add(segmentPoint1);

        // Calcular cada ángulo.
        float firstAngle = calculateSweepAngleFromThreePoints(firstAnglePoints);
        float secondAngle = calculateSweepAngleFromThreePoints(secondAnglePoints);

        /*
        Verificar si alguno de los ángulos es obtuso. En caso de serlo, se calculará la distancia entre el
        punto fijo y el vértice del ángulo obtuso.
         */
        if (firstAngle > 90) {
            return distanceBetweenPoints(segmentPoint1.x, segmentPoint1.y, point.x, point.y);
        }
        if (secondAngle > 90) {
            return distanceBetweenPoints(segmentPoint2.x, segmentPoint2.y, point.x, point.y);
        }

        // Calcular distancia entre el punto fijo y la recta formada por los extremos del segmento.
        return distanceBetweenLineAndPoint(point, segmentPoint1, segmentPoint2);
    }

    /**
     * Traslada un punto final a lo largo de la dirección de la línea formada por este y el punto inicial,
     * hasta que se encuentre a la distancia especificada del punto inicial.
     * @param initialPoint Punto inicial.
     * @param endPoint Punto final que será trasladado.
     * @param distance Distancia a la que debe ser trasladado el punto final.
     * @param minimumDistance Cuando se especifica en True, este parámetro determina que el punto final no
     *                        será trasladado en caso de que la distancia entre los puntos proporcionados
     *                        sea mayor a la distancia indicada como parámetro.
     * @return El nuevo punto final trasladado según los requerimientos.
     */
    public static PointF extendEndPointToDistance(PointF initialPoint, PointF endPoint,
                                                  float distance, boolean minimumDistance) {
        // Calcular la distancia existente entre los puntos.
        float pointsDistance = distanceBetweenPoints(initialPoint.x, initialPoint.y, endPoint.x,
            endPoint.y);

        if (minimumDistance && pointsDistance > distance) {
            return endPoint;
        }

        // Calcular el vector entre el punto de inicio y el de fin.
        PointF vector = new PointF(endPoint.x - initialPoint.x, endPoint.y - initialPoint.y);

        // Normalizar el vector.
        vector.x = vector.x / pointsDistance;
        vector.y = vector.y / pointsDistance;

        // Multiplicar el vector de longitud 1 por la distancia.
        vector.x *= distance;
        vector.y *= distance;

        // Calcular nuevo punto final sumando las coordenadas del punto inicial y el vector calculado.
        return new PointF(initialPoint.x + vector.x, initialPoint.y + vector.y);
    }

    public static float[] calculateCartesianAxesFromThreePoints(PointF firstPoint, PointF secondPoint,
                                                             PointF thirdPoint) {
        // Calcular pendiente de la recta que atraviesa el primer y tercer punto (Eje X).
        float xAxisSlope = getSlopeFromTwoPoints(firstPoint, thirdPoint);
        // Calcular pendiente del Eje Y. Al ser perpendicular al Eje X, su pendiente es -1/pendienteX.
        float yAxisSlope = -1 / xAxisSlope;

        // Calcular ordenada al origen de cada recta.
        float xAxisIntercept = getInterceptFromSlopeAndPoint(xAxisSlope, secondPoint);
        float yAxisIntercept = getInterceptFromSlopeAndPoint(yAxisSlope, secondPoint);

        int distance = 10000;
        PointF endPointX = extendEndPointToDistance(secondPoint,
            new PointF(secondPoint.x + 1, xAxisSlope * (secondPoint.x + 1) + xAxisIntercept),
            distance, false);
        PointF initialPointX = extendEndPointToDistance(secondPoint, endPointX, -distance, false);

        PointF endPointY = extendEndPointToDistance(secondPoint,
            new PointF(secondPoint.x + 1, yAxisSlope * (secondPoint.x + 1) + yAxisIntercept),
            distance, false);
        PointF initialPointY = extendEndPointToDistance(secondPoint, endPointY, -distance, false);

        return new float[] { initialPointX.x, initialPointX.y, endPointX.x, endPointX.y,
            initialPointY.x, initialPointY.y, endPointY.x, endPointY.y };
    }

    /**
     * A partir de datos de una circunferencia y un punto 'P', calcula el punto 'Q' más cercano a P que se
     * encuentra sobre la circunferencia.
     * @param center Centro de la circunferencia.
     * @param radius Radio de la circunferencia.
     * @param point Punto a trasladar.
     * @return Punto más cercano a P que se encuentra sobre la circunferencia indicada.
     */
    public static PointF translatePointToCircumference(PointF center, float radius, PointF point) {
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

    private static float getSlopeFromTwoPoints(PointF firstPoint, PointF secondPoint) {
        return (secondPoint.y - firstPoint.y) / (secondPoint.x - firstPoint.x);
    }

    private static float getInterceptFromSlopeAndPoint(float slope, PointF point) {
        return point.y - (slope * point.x);
    }
}