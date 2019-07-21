package barsotti.alejandro.tf.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import barsotti.alejandro.tf.BuildConfig;
import barsotti.alejandro.tf.R;

public class ImageUtils {
    private static final String TAG = "ImageUtils";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd_hh-mm-ss",
        Locale.getDefault());
    private static MediaScannerConnection mediaScannerConnection;

    public static Uri createImageFile(Context context) {
        // Crear un nombre único para el archivo de imagen.
        Date dateTime = new Date();
        String photoFileName = context.getText(R.string.app_name) + dateFormat.format(dateTime);

        // Obtener el directorio de salida para la imagen.
        File outputDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        // Crear y devolver el archivo de imagen.
        try {
            File tempFile = File.createTempFile(photoFileName, context.getString(R.string.photo_file_format),
                outputDirectory);

//            return Uri.fromFile(tempFile);

            return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider",
                tempFile);
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }

    public static Uri takeAndStoreScreenshot(Context context, View view) {
        Bitmap bitmap = takeScreenshot(view);
        String filename = generateFilenameForScreenshot();

        try {
            File screenshotFile = storeScreenshot(bitmap, filename);

            addFileToMediaScannerService(context, screenshotFile.getPath());

//            return Uri.fromFile(screenshotFile);
            return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider",
                screenshotFile);
        } catch (IOException e) {
            Log.e(TAG, "takeAndStoreScreenshot: " +
                view.getResources().getString(R.string.storeScreenshot_error_message));
            return null;
        }
    }

    public static void addFileToMediaScannerService(Context context, final String screenshotPath) {
        mediaScannerConnection = new MediaScannerConnection(context, new MediaScannerConnectionClient() {
            public void onScanCompleted(String path, Uri uri) {
                if (mediaScannerConnection.isConnected()) {
                    mediaScannerConnection.disconnect();
                }
            }
            public void onMediaScannerConnected() {
                mediaScannerConnection.scanFile(screenshotPath, null);
            }
        });
        mediaScannerConnection.connect();
    }

    private static String generateFilenameForScreenshot() {
        Date date = new Date();

        return String.format("Measurement-Result-Screenshot_%s.png", dateFormat.format(date));
    }

    private static Bitmap takeScreenshot(View v) {
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache(true);
        Bitmap b = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);
        return b;
    }

    private static File storeScreenshot(Bitmap bitmap, String filename) throws IOException {
        String basePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            .toString();

        File imageFile = new File(String.format("%s/%s", basePath, filename));

        OutputStream out = new FileOutputStream(imageFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.flush();
        out.close();

        return imageFile;
    }
}
