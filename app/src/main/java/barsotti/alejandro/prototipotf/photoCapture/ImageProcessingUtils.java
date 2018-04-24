package barsotti.alejandro.prototipotf.photoCapture;

import android.graphics.Bitmap;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class ImageProcessingUtils {
    private static final int OPENCV_MIN_EDGE_THRESHOLD = 70;
    private static final int OPENCV_MAX_EDGE_THRESHOLD = 90;

    private static Bitmap detectEdges(Bitmap bitmap) {
        // Crear una matriz con la información de la imagen.
        Mat rgba = new Mat();
        Utils.bitmapToMat(bitmap, rgba);

        // Utilizar OpenCV para detectar bordes y almacenar la información en una nueva matriz.
        Mat edges = new Mat(rgba.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(rgba, edges, Imgproc.COLOR_RGB2GRAY, 4);
        Imgproc.Canny(edges, edges, OPENCV_MIN_EDGE_THRESHOLD, OPENCV_MAX_EDGE_THRESHOLD);

        // Utilizar la información de detección de bordes provista por OpenCV para generar un nuevo bitmap.
        Bitmap resultBitmap = Bitmap.createBitmap(edges.cols(), edges.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(edges, resultBitmap);

        return resultBitmap;
    }
}
