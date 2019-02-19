package barsotti.alejandro.prototipotf.utils;

import android.graphics.Bitmap;
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

    public static File takeAndStoreScreenshot(View view) {
        Bitmap bitmap = takeScreenshot(view);
        String filename = generateFilenameForScreenshot();

        try {
            return storeScreenshot(bitmap, filename);
        } catch (IOException e) {
            Log.e(TAG, "takeAndStoreScreenshot: " +
                view.getResources().getString(R.string.storeScreenshot_error_message));
            return null;
        }
    }

    private static String generateFilenameForScreenshot() {
        Date date = new Date();

        return String.format("Measurement-Screenshot_%s.png", dateFormat.format(date));
    }

    private static Bitmap takeScreenshot(View v) {
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache(true);
        Bitmap b = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);
        return b;
    }

    private static File storeScreenshot(Bitmap bitmap, String filename) throws IOException {
        String path = Environment.getExternalStorageDirectory().toString() + "/" + filename;
        File imageFile = new File(path);

        OutputStream out = new FileOutputStream(imageFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.flush();
        out.close();

        return imageFile;
    }

}
