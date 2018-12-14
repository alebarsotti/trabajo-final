package barsotti.alejandro.prototipotf.Utils;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.util.Log;

import java.util.ArrayList;

import barsotti.alejandro.prototipotf.customViews.Circle;

public class MathUtils {
    // Valor de tolerancia utilizado para la realización de cálculos precisos.
    private static double TOLERANCE = 0.0001d;
    // Tag utilizado a efectos de debug.
    private static String TAG = "MathUtils";
    private static int DEFAULT_DISTANCE_BETWEEN_POINTS_IN_LINE = 50;
    public static int MAX_NUMBER_OF_POINTS_PER_LINE = 100;

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
     * Determina si el valor proporcionado se encuentra dentro de los límites establecidos.
     * @param value Valor a controlar.
     * @param lowerLimit Límite inferior (inclusivo).
     * @param upperLimit Límite superior (inclusivo).
     * @return True si el valor se encuentra dentro del intervalo. False en caso contrario.
     */
    public static boolean valueWithinRange(double value, double lowerLimit, double upperLimit) {
        return value >= lowerLimit && value <= upperLimit;
    }

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

    /**
     * Calcula centro y radio de la circunferencia a partir de tres puntos dados. Asimismo, establece
     * la lista de N puntos que conforman la circunferencia, siendo N =
     * {@link Circle#NUMBER_OF_POINTS_TO_DRAW}
     * @param circle La instancia de circunferencia para la cual realizar los cálculos.
     */
    public static void circumferenceFromThreePoints(Circle circle) {
        ArrayList<PointF> points = circle.getPointArray();

        float point1X, point1Y, point2X, point2Y, point3X, point3Y, delta1, delta2, point12X, point12Y,
            point23X, point23Y, x, y, radius;

        // Separar coordenadas de los puntos de la circunferencia con el fin de utilizarlos fácilmente.
        point1X = points.get(0).x;
        point1Y = points.get(0).y;
        point2X = points.get(1).x;
        point2Y = points.get(1).y;
        point3X = points.get(2).x;
        point3Y = points.get(2).y;

        // Calcular deltas entre puntos.
        delta1 = (point2X - point1X) / (point2Y - point1Y);
        delta2 = (point3X - point2X) / (point3Y - point2Y);

        // Controlar delta2 - delta1 != 0. De lo contrario, no se podría calcular la circunferencia.
        if (Math.abs(delta2 - delta1) < TOLERANCE) {
            // El cálculo no se puede realizar. Devolver circunferencia por defecto.
            circle.setCenterAndRadius(new PointF(0, 0), 0);

            return;
        }

        // Calcular punto intermedio entre los puntos 1 y 2, y entre los puntos 2 y 3. Estos puntos indican
        // la ubicación de los bisectores perpendiculares de las líneas que unen cada par de puntos.
        point12X = (point1X + point2X) / 2;
        point12Y = (point1Y + point2Y) / 2;
        point23X = (point2X + point3X) / 2;
        point23Y = (point2Y + point3Y) / 2;

        // Calcular coordenada X del centro de la circunferencia.
        x = (point23Y + point23X * delta2 - point12Y - point12X * delta1) / (delta2 - delta1);

        // Calcular coordenada Y del centro de la circunferencia.
        y = -1 * x * delta1 + point12Y + point12X * delta1;

        // Calcular radio.
        radius = (float) Math.sqrt(Math.pow(x - point1X, 2) + Math.pow(y - point1Y, 2));

        // Establecer centro y radio de la circunferencia.
        circle.setCenterAndRadius(new PointF(x, y), radius);

        // Crear path que representa la circunferencia mediante curvas Bézier.
        float bezierCurveConstant = 0.551915024494f;
        float deltaM = bezierCurveConstant * radius;
        Path newPath = new Path();
        newPath.moveTo(x, y + radius);
        newPath.cubicTo(x + deltaM, y + radius, x + radius, y + deltaM, x + radius, y);
        newPath.cubicTo(x + radius, y - deltaM, x + deltaM, y - radius, x, y - radius);
        newPath.cubicTo(x - deltaM, y - radius, x - radius, y - deltaM, x - radius, y);
        newPath.cubicTo(x - radius, y + deltaM, x - deltaM, y + radius, x, y + radius);
        newPath.close();

        // Calcular coordenadas de los puntos que forman la circunferencia.
        int numberOfPoints = Circle.NUMBER_OF_POINTS_TO_DRAW;
        PathMeasure pm = new PathMeasure(newPath, false);
        float distance = 0f;
        float deltaDistance = pm.getLength() / numberOfPoints;
        int pointArrayPosition = 0;
        float[] pointCoordinates = new float[2];
        float[] pointCoordinatesArray = new float[numberOfPoints * 4];

        for (int counter = 0; counter < numberOfPoints; counter++) {
            pm.getPosTan(distance, pointCoordinates, null);
            pointCoordinatesArray[pointArrayPosition++] = pointCoordinates[0];
            pointCoordinatesArray[pointArrayPosition++] = pointCoordinates[1];
            if (counter != 0) {
                pointCoordinatesArray[pointArrayPosition++] = pointCoordinates[0];
                pointCoordinatesArray[pointArrayPosition++] = pointCoordinates[1];
            }
            else {
                pointCoordinatesArray[pointCoordinatesArray.length - 2] = pointCoordinates[0];
                pointCoordinatesArray[pointCoordinatesArray.length - 1] = pointCoordinates[1];
            }

            distance = distance + deltaDistance;
        }

        // Establecer la lista de puntos.
        circle.setPathPoints(pointCoordinatesArray);
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
        double firstEndVertexDistance = distanceBetweenPoints(firstEnd.x, firstEnd.y, vertex.x, vertex.y);
        double secondEndVertexDistance = distanceBetweenPoints(secondEnd.x, secondEnd.y, vertex.x, vertex.y);
        double firstEndSecondEndDistance = distanceBetweenPoints(firstEnd.x, firstEnd.y,
            secondEnd.x, secondEnd.y);

        // Aplicar Ley de los Cosenos.
        double angle = Math.toDegrees(Math.acos((Math.pow(secondEndVertexDistance, 2)
            + Math.pow(firstEndVertexDistance, 2) - Math.pow(firstEndSecondEndDistance, 2))
            / 2 / secondEndVertexDistance / firstEndVertexDistance)
        );

        Log.d(TAG, "calculateSweepAngleFromThreePoints: " + angle);
        return (float) angle;
    }

    /**
     * Calcula el ángulo de inicio necesario para la representación gráfica de un arco dentro de ángulo
     * formado por los tres puntos proporcionados.
     * @param points ArrayList de los tres puntos que forman el ángulo.
     * @return Magnitud del ángulo de inicio requerido para la representación gráfica del arco.
     */
    public static float calculateStartAngleFromThreePoints(ArrayList<PointF> points) {
        // Obtener extremos y vértice.
        PointF firstEnd = points.get(0);
        PointF vertex = points.get(1);
        PointF secondEnd = points.get(2);

        // Establecer punto de inicio de referencia. El mismo se encontrará siempre una unidad a la derecha
        // del vértice, y a la misma altura y. Esto resultará útil para calcular el startAngle desde el
        // origen considerado por Android (eje X positivo) hasta cada extremo.
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

        // Calcular cada ángulo. En caso de encontrarse por encima del punto de inicio de referencia, el
        // valor a considerar para ese ángulo será la resta entre 360º y el valor obtenido. Esto se debe a
        // que Android dibuja los ángulos desde el punto de inicio de referencia y en sentido horario).
        float firstStartAngle = calculateSweepAngleFromThreePoints(firstStartAnglePoints);
        firstStartAngle = (firstEnd.y < startPoint.y ? 360 - firstStartAngle : firstStartAngle);
        float secondStartAngle = calculateSweepAngleFromThreePoints(secondStartAnglePoints);
        secondStartAngle = (secondEnd.y < startPoint.y ? 360 - secondStartAngle : secondStartAngle);

        // TODO: Limpiar.
        // Calcular diferencia entre los ángulos.
        float diff = Math.max(firstStartAngle, secondStartAngle) - Math.min(firstStartAngle, secondStartAngle);
//        float diff = Math.abs(firstStartAngle - secondStartAngle);

        // Si la diferencia es menor a 180º, entonces el ángulo de inicio es el ángulo que culmina en el
        // primer extremo. En caso contrario, será el ángulo que culmina en el segundo extremo.
        return diff < 180 ? Math.min(firstStartAngle, secondStartAngle) : Math.max(firstStartAngle, secondStartAngle);
//        return diff < 180 ? firstStartAngle : secondStartAngle;
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

        // Verificar si alguno de los ángulos es obtuso. En caso de serlo, se calculará la distancia entre
        // el punto fijo y el vértice del ángulo obtuso.
        if (firstAngle > 90) {
            return distanceBetweenPoints(segmentPoint1.x, segmentPoint1.y, point.x, point.y);
        }
        if (secondAngle > 90) {
            return distanceBetweenPoints(segmentPoint2.x, segmentPoint2.y, point.x, point.y);
        }

        // Calcular distancia entre el punto fijo y la recta formada por los extremos del segmento.
        return distanceBetweenLineAndPoint(point, segmentPoint1, segmentPoint2);
    }

    public static float[] pointArrayFromLine(PointF point1, PointF point2) {
        Path linePath = new Path();
        linePath.moveTo(point1.x, point1.y);
        linePath.lineTo(point2.x, point2.y);

        PathMeasure pm = new PathMeasure(linePath, false);

        float distance = Math.max(pm.getLength() / MAX_NUMBER_OF_POINTS_PER_LINE,
            DEFAULT_DISTANCE_BETWEEN_POINTS_IN_LINE);

        int numberOfPointsNeeded = (int) (pm.getLength() / distance) + 1;
        float currentDistance = 0;
        float[] pointCoordinates = new float[2];
        float[] pointArray = new float[numberOfPointsNeeded * 2];

        for (int i = 0; i < numberOfPointsNeeded; i++) {
            pm.getPosTan(currentDistance, pointCoordinates, null);

            pointArray[2 * i] = pointCoordinates[0];
            pointArray[2 * i + 1] = pointCoordinates[1];

            currentDistance += distance;
        }

        return pointArray;
    }

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

    public static PointF getCoordinatesForTextDrawing(PointF vertex, PointF firstEnd, PointF secondEnd) {
        //TODO: Normalizar antes de calcular el punto de la bisección.
//        PointF pointInBisection = new PointF(firstEnd.x + secondEnd.x - vertex.x,
//            firstEnd.x + secondEnd.x - vertex.x);

        // Calcular el vector entre el punto de inicio y el de fin.
        PointF firstVector = new PointF(firstEnd.x - vertex.x, firstEnd.y - vertex.y);
        float firstVectorLength = distanceBetweenPoints(firstEnd.x, firstEnd.y, vertex.x, vertex.y);
        PointF secondVector = new PointF(secondEnd.x - vertex.x, secondEnd.y - vertex.y);
        float secondVectorLength = distanceBetweenPoints(secondEnd.x, secondEnd.y, vertex.x, vertex.y);

        // Normalizar el vector.
        firstVector.x /= firstVectorLength;
        firstVector.y /= firstVectorLength;
        secondVector.x /= secondVectorLength;
        secondVector.y /= secondVectorLength;

        PointF pointInBisection = new PointF(firstVector.x + secondVector.x + vertex.x,
            firstVector.y + secondVector.y + vertex.y);

        return extendEndPointToDistance(vertex, pointInBisection, -100, false);
    }
}