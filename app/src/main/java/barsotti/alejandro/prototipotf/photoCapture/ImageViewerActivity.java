package barsotti.alejandro.prototipotf.photoCapture;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.Toast;

import java.io.IOException;

import barsotti.alejandro.prototipotf.R;
import barsotti.alejandro.prototipotf.customViews.ZoomableImageView;

public class ImageViewerActivity extends AppCompatActivity {
    public static final String BITMAP_URI_EXTRA = "bitmapUri";

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ZoomableImageView mZoomableImageView;
//    private Bitmap mBitmap;
    private Uri mBitmapUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_viewer);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Toast.makeText(ImageViewerActivity.this, "Item Selected: " + item.getTitle(), Toast.LENGTH_SHORT).show();

                mDrawerLayout.closeDrawers();

                boolean edgesOnly = false;
                switch (item.getItemId()) {
                    case R.id.menu_seccion_1:
//                        mZoomableImageView.setBitmap(mBitmap);
                        setBitmap();
//                        edgesOnly = false;
                        break;
//                    case R.id.menu_seccion_2:
////                        Bitmap bitmap = ImageProcessingUtils.detectEdges(mBitmap);
////                        mZoomableImageView.setBitmap(bitmap);
//                        edgesOnly = true;
//                        break;
                    case R.id.menu_seccion_2: {
                        setBitmap(80, 100);
                        break;
                    }

                    case R.id.menu_seccion_3: {
                        setBitmap(60, 70);
                        break;
                    }

                    case R.id.menu_seccion_4: {
                        setBitmap(60, 80);
                        break;
                    }

                    case R.id.menu_seccion_5: {
                        setBitmap(60, 90);
                        break;
                    }

                    case R.id.menu_seccion_6: {
                        setBitmap(60, 100);
                        break;
                    }

                    case R.id.menu_seccion_7: {
                        setBitmap(70, 80);
                        break;
                    }

                    case R.id.menu_seccion_8: {
                        setBitmap(70, 90);
                        break;
                    }

                    case R.id.menu_seccion_9: {
                        setBitmap(70, 100);
                        break;
                    }

                    case R.id.menu_seccion_10: {
                        setBitmap(80, 90);
                        break;
                    }

                    case R.id.menu_seccion_11: {
                        setBitmap(80, 100);
                        break;
                    }

                    case R.id.menu_seccion_12: {
                        setBitmap(90, 100);
                        break;
                    }

                }
                item.setChecked(true);
//                setBitmap(edgesOnly);

                return true;
            }
        });

        mZoomableImageView = findViewById(R.id.zoomable_image_view);
        Intent intent = getIntent();
//        Uri bitmapUri = intent.getParcelableExtra(BITMAP_URI_EXTRA);
        mBitmapUri = intent.getParcelableExtra(BITMAP_URI_EXTRA);
        setBitmap();
    }

    private void setBitmap() {
        setBitmap(false, 0, 0);
    }

    private void setBitmap(int minThreshold, int maxThreshold) {
        setBitmap(true, minThreshold, maxThreshold);
    }

    private void setBitmap(boolean edgesOnly, int minThreshold, int maxThreshold) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mBitmapUri);
            if (edgesOnly) {
                if (minThreshold != 0) {
                    bitmap = ImageProcessingUtils.detectEdges(bitmap, minThreshold, maxThreshold);
                }
                else {
                    bitmap = ImageProcessingUtils.detectEdges(bitmap);
                }
            }
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
