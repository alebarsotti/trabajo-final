package barsotti.alejandro.prototipotf.Utils;

import android.graphics.PointF;
import android.util.Log;

import java.util.ArrayList;

import barsotti.alejandro.prototipotf.customViews.Circle;

public class MathUtils {
    private static String TAG = "MathUtils";
    private static double TOLERANCE = 0.0001;
    private static double TANGENT_POINT_DISTANCE = 2000;

    public static double distanceBetweenPoints(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public static boolean valueWithinRange(double value, double lowerLimit, double upperLimit) {
        return value >= lowerLimit && value <= upperLimit;
    }

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

    public static ArrayList<PointF> tangentToCircumference(PointF center, PointF pointInCircumference) {
        ArrayList<PointF> tangent = new ArrayList<>();

        // Determinar si la diferencia en la coordenada "y" de los puntos es cero (caso especial).
        if (Math.abs(pointInCircumference.y - center.y) < TOLERANCE) {
            tangent.add(new PointF(pointInCircumference.x,
                (float) (pointInCircumference.y + TANGENT_POINT_DISTANCE)));
            tangent.add(new PointF(pointInCircumference.x,
                (float) (pointInCircumference.y - TANGENT_POINT_DISTANCE)));
        }
        else {
            // Los nombres de variables "a" y "b" hacen referencia a la fórmula de la recta: y = a * x + b.
            double a = -1 * (pointInCircumference.x - center.x) / (pointInCircumference.y - center.y);
            double b = pointInCircumference.y - pointInCircumference.x * a;

            double firstPointX = pointInCircumference.x - TANGENT_POINT_DISTANCE;
            double firstPointY = a * firstPointX + b;
            double secondPointX = pointInCircumference.x + TANGENT_POINT_DISTANCE;
            double secondPointY = a * secondPointX + b;

            tangent.add(new PointF((float) firstPointX, (float) firstPointY));
            tangent.add(new PointF((float) secondPointX, (float) secondPointY));
        }

        return tangent;
    }

    public static void circumferenceFromThreePoints(Circle circle) {
        ArrayList<PointF> points = circle.getPoints();

        double point1X, point1Y, point2X, point2Y, point3X, point3Y, delta1, delta2, point12X, point12Y,
            point23X, point23Y, x, y, radius;

        point1X = points.get(0).x;
        point1Y = points.get(0).y;
        point2X = points.get(1).x;
        point2Y = points.get(1).y;
        point3X = points.get(2).x;
        point3Y = points.get(2).y;

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
        radius = Math.sqrt(Math.pow(x - point1X, 2) + Math.pow(y - point1Y, 2));

        // Establecer resultados.
        circle.setCenterAndRadius(new PointF((float) x, (float) y), (float) radius);
    }
}
