package barsotti.alejandro.prototipotf.photoCapture;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;

public class ImageProcessingUtils {
//    private static final int OPEN_CV_EDGE_THRESHOLD1 = 30;
    private static final int OPEN_CV_EDGE_THRESHOLD1 = 60;
    // La recomendación es que el threshold2 sea el triple del threshold1.
//    private static final int OPEN_CV_EDGE_THRESHOLD2 = OPEN_CV_EDGE_THRESHOLD1 * 3;
    private static final int OPEN_CV_EDGE_THRESHOLD2 = 80;
    private static final Size OPEN_CV_BLUR_KERNEL_SIZE = new Size(3, 3);
    private static final int OPEN_CV_CANNY_APERTURE_SIZE = 3;
    private static final boolean OPEN_CV_CANNY_USE_L2_GRADIENT = true;

    public static Uri detectEdges(String originalBitmapPath, String filename, Context context) {
        // Generar matrix a partir del path de la imagen.
        Mat originalMat = Imgcodecs.imread(originalBitmapPath);

        // Transformar la matriz a escala de grises.
        Mat grayScaleMat = new Mat();
        Imgproc.cvtColor(originalMat, grayScaleMat, Imgproc.COLOR_BGR2GRAY);

        // Aplicar blur a matriz en escala de grises.
        Mat blurredGrayScaleMat = new Mat();
        Imgproc.blur(grayScaleMat, blurredGrayScaleMat, OPEN_CV_BLUR_KERNEL_SIZE);

        // Detectar bordes en la matriz.
        Mat edgesOnlyMat = new Mat();
        Imgproc.Canny(blurredGrayScaleMat, edgesOnlyMat, OPEN_CV_EDGE_THRESHOLD1, OPEN_CV_EDGE_THRESHOLD2,
            OPEN_CV_CANNY_APERTURE_SIZE, OPEN_CV_CANNY_USE_L2_GRADIENT);

        // Crear archivo.
        File edgesBitmap = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);

        // Guardar archivo de imagen.
        Imgcodecs.imwrite(edgesBitmap.getAbsolutePath(), edgesOnlyMat);

        // Agregar a MediaStore para que pueda visualizarse en la galería.
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), edgesBitmap.getAbsolutePath(),
                edgesBitmap.getName(), "Edges Only Image");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return Uri.fromFile(edgesBitmap);
    }
}
