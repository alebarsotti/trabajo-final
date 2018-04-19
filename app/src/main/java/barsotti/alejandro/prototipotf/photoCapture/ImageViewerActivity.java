package barsotti.alejandro.prototipotf.photoCapture;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

import java.io.IOException;

import barsotti.alejandro.prototipotf.R;
import barsotti.alejandro.prototipotf.customViews.ZoomableImageView;

public class ImageViewerActivity extends AppCompatActivity {
    public static final String BITMAP_URI_EXTRA = "bitmapUri";

    private ZoomableImageView mZoomableImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_viewer);

        mZoomableImageView = findViewById(R.id.zoomable_image_view);
        Intent intent = getIntent();
        Uri bitmapUri = intent.getParcelableExtra(BITMAP_URI_EXTRA);
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), bitmapUri);
            mZoomableImageView.setBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        View decorView = getWindow().getDecorView();
        if (hasFocus) {
            decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

            );
        }
    }
}
