package barsotti.alejandro.prototipotf.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import barsotti.alejandro.prototipotf.R;

public class ScreenshotUtils {
    private static final String TAG = "ScreenshotUtils";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd_hh-mm-ss",
        Locale.getDefault());
    private static MediaScannerConnection mediaScannerConnection;

    public static Uri takeAndStoreScreenshot(Context context, View view) {
        Bitmap bitmap = takeScreenshot(view);
        String filename = generateFilenameForScreenshot();

        try {
            File screenshotFile = storeScreenshot(bitmap, filename);

            addFileToMediaScannerService(context, screenshotFile.getPath());

            return Uri.fromFile(screenshotFile);
        } catch (IOException e) {
            Log.e(TAG, "takeAndStoreScreenshot: " +
                view.getResources().getString(R.string.storeScreenshot_error_message));
            return null;
        }
    }

    private static void addFileToMediaScannerService(Context context, final String screenshotPath) {
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
