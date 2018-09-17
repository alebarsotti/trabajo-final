package barsotti.alejandro.prototipotf.photoCapture;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;

import barsotti.alejandro.prototipotf.R;
import barsotti.alejandro.prototipotf.customViews.ZoomableImageView;
import barsotti.alejandro.prototipotf.customViews.ZoomableImageViewGroup;

public class ImageViewerActivity extends AppCompatActivity {
    public static final String BITMAP_URI_EXTRA = "bitmapUri";
    public static final String BITMAP_EDGES_URI_EXTRA = "bitmapEdgesUri";

    private Point mScreenSize = new Point();

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
//    private ZoomableImageView mZoomableImageView;
    private ZoomableImageViewGroup mZoomableImageViewGroup;
    private Uri mBitmapUri;
    private Uri mBitmapEdgesUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.activity_image_viewer);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(new ItemSelectedLister());

//        mZoomableImageView = findViewById(R.id.zoomable_image_view);
        mZoomableImageViewGroup = findViewById(R.id.zoomable_image_view_group);

        Intent intent = getIntent();
        mBitmapUri = intent.getParcelableExtra(BITMAP_URI_EXTRA);
        mBitmapEdgesUri = intent.getParcelableExtra(BITMAP_EDGES_URI_EXTRA);

        setBitmap();
        mNavigationView.getMenu().findItem(R.id.original_image).setChecked(true);

        super.onCreate(savedInstanceState);
    }

    private class ItemSelectedLister implements NavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            mDrawerLayout.closeDrawers();

            switch (item.getItemId()) {
                case R.id.detected_edges: {
                    setBitmap(true);
                    mZoomableImageViewGroup.setZoomableImageViewState(ZoomableImageView.State.None);
                    item.setChecked(true);
                    break;
                }
                case R.id.original_image: {
                    setBitmap(false);
                    mZoomableImageViewGroup.setZoomableImageViewState(ZoomableImageView.State.None);
                    item.setChecked(true);
                    break;
                }
                case R.id.draw: {
//                    mZoomableImageViewGroup.setZoomableImageViewState(ZoomableImageView.State.Drawing);
                    mZoomableImageViewGroup.setZoomableImageViewDrawingInProgress(true);
                    break;
                }
            }

            return true;
        }
    }

    private void setBitmap() {
        setBitmap(false);
    }

    private void setBitmap(final boolean edgesOnly) {
        getWindowManager().getDefaultDisplay().getRealSize(mScreenSize);

        RequestOptions glideOptions = new RequestOptions()
            .fitCenter()
            .override(mScreenSize.x, mScreenSize.y);

        Glide.with(this)
            .load(edgesOnly ? mBitmapEdgesUri : mBitmapUri)
            .apply(glideOptions)
            .into(new ViewTarget<ZoomableImageViewGroup, Drawable>(mZoomableImageViewGroup) {
                @Override
                public void onResourceReady(@NonNull Drawable resource,
                                            @Nullable Transition<? super Drawable> transition) {
                    mZoomableImageViewGroup.setupZoomableImageView(mScreenSize.x, mScreenSize.y,
                        edgesOnly ? mBitmapEdgesUri : mBitmapUri, resource);
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