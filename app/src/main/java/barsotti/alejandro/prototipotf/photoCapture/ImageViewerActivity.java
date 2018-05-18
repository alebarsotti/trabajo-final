package barsotti.alejandro.prototipotf.photoCapture;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.MenuItem;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;

import barsotti.alejandro.prototipotf.R;
import barsotti.alejandro.prototipotf.customViews.ZoomableImageView;

public class ImageViewerActivity extends AppCompatActivity {
    public static final String BITMAP_URI_EXTRA = "bitmapUri";
    public static final String BITMAP_EDGES_URI_EXTRA = "bitmapEdgesUri";
    public static final String BITMAP_WIDTH = "bitmapWidth";
    public static final String BITMAP_HEIGHT = "bitmapHeight";


    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ZoomableImageView mZoomableImageView;
//    private Bitmap mBitmap;
    private Uri mBitmapUri;
    private Uri mBitmapEdgesUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_viewer);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                mDrawerLayout.closeDrawers();

                boolean edgesOnly = false;
                if (item.getItemId() == R.id.bordes_detectados) {
                    edgesOnly = true;
                }

                item.setChecked(true);
                setBitmap(edgesOnly);

                return true;
            }
        });

        mZoomableImageView = findViewById(R.id.zoomable_image_view);
        Intent intent = getIntent();
        mBitmapUri = intent.getParcelableExtra(BITMAP_URI_EXTRA);
        mBitmapEdgesUri = intent.getParcelableExtra(BITMAP_EDGES_URI_EXTRA);

        setBitmap();
        mNavigationView.getMenu().findItem(R.id.imagen_original).setChecked(true);
    }

    private void setBitmap() {
        setBitmap(false);
    }

    private void setBitmap(final boolean edgesOnly) {
        // Método malo pero sin perder calidad.
//        mZoomableImageView.setImageURI(edgesOnly ? mBitmapEdgesUri : mBitmapUri);
//        Point screenSize = new Point();
//        getWindowManager().getDefaultDisplay().getRealSize(screenSize);
//        mZoomableImageView.setScale(screenSize.x, screenSize.y);

        // Método mejor pero perdiendo calidad.
        Glide.with(this)
            .load(edgesOnly ? mBitmapEdgesUri : mBitmapUri)
            .into(new ViewTarget<ZoomableImageView, Drawable>(mZoomableImageView) {

                @Override
                public void onResourceReady(@NonNull Drawable resource,
                                            @Nullable Transition<? super Drawable> transition) {
                    mZoomableImageView.setImageDrawable(resource);
                    Point screenSize = new Point();
                    getWindowManager().getDefaultDisplay().getRealSize(screenSize);
                    mZoomableImageView.setScale(screenSize.x, screenSize.y);
                }
            });
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
