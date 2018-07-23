package barsotti.alejandro.prototipotf.photoCapture;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
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

import java.io.IOException;
import java.io.InputStream;

import barsotti.alejandro.prototipotf.R;
import barsotti.alejandro.prototipotf.customViews.ZoomableImageView;
import barsotti.alejandro.prototipotf.interfaces.IZoomableImageView;

public class ImageViewerActivity extends AppCompatActivity {
    public static final String BITMAP_URI_EXTRA = "bitmapUri";
    public static final String BITMAP_EDGES_URI_EXTRA = "bitmapEdgesUri";
    public static final String BITMAP_WIDTH = "bitmapWidth";
    public static final String BITMAP_HEIGHT = "bitmapHeight";

    private Point mScreenSize;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ZoomableImageView mZoomableImageView;
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

//                boolean edgesOnly = false;
//                if (item.getItemId() == R.id.bordes_detectados) {
//                    edgesOnly = true;
//                }
//
//                item.setChecked(true);
//                setBitmap(edgesOnly);

                switch (item.getItemId()) {
                    case R.id.detected_edges: {
                        setBitmap(true);
                        mZoomableImageView.setState(ZoomableImageView.States.None);
                        item.setChecked(true);
                        break;
                    }
                    case R.id.original_image: {
                        setBitmap(false);
                        mZoomableImageView.setState(ZoomableImageView.States.None);
                        item.setChecked(true);
                        break;
                    }
                    case R.id.draw: {
                        mZoomableImageView.setState(ZoomableImageView.States.Drawing);
                        break;
                    }
                }

                return true;
            }
        });

        mZoomableImageView = findViewById(R.id.zoomable_image_view);
        Intent intent = getIntent();
        mBitmapUri = intent.getParcelableExtra(BITMAP_URI_EXTRA);
        mBitmapEdgesUri = intent.getParcelableExtra(BITMAP_EDGES_URI_EXTRA);

        mScreenSize = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(mScreenSize);

        setBitmap();
        mNavigationView.getMenu().findItem(R.id.original_image).setChecked(true);
    }

    private void setBitmap() {
        setBitmap(false);
    }

    private void setBitmap(final boolean edgesOnly) {
        // Nuevo método quizá mejor (?)
        RequestOptions glideOptions = new RequestOptions()
            .fitCenter()
            .override(mScreenSize.x, mScreenSize.y);

        Glide.with(this)
            .load(edgesOnly ? mBitmapEdgesUri : mBitmapUri)
            .apply(glideOptions)
            .into(new ViewTarget<ZoomableImageView, Drawable>(mZoomableImageView) {
                @Override
                public void onResourceReady(@NonNull Drawable resource,
                                            @Nullable Transition<? super Drawable> transition) {
                    mZoomableImageView.setImageDrawable(resource);
                    mZoomableImageView.setScale(mScreenSize.x, mScreenSize.y);
                    mZoomableImageView.setImageUri(edgesOnly ? mBitmapEdgesUri : mBitmapUri);
                }
            });

//        // Método malo pero sin perder calidad.
//        mZoomableImageView.setImageURI(edgesOnly ? mBitmapEdgesUri : mBitmapUri);


//        // Método mejor pero perdiendo calidad.
//        Glide.with(this)
//            .load(edgesOnly ? mBitmapEdgesUri : mBitmapUri)
//            .into(new ViewTarget<ZoomableImageView, Drawable>(mZoomableImageView) {
//
//                @Override
//                public void onResourceReady(@NonNull Drawable resource,
//                                            @Nullable Transition<? super Drawable> transition) {
//                    mZoomableImageView.setImageDrawable(resource);
//                }
//            });
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


    public static class AsyncImageRegionDecoder extends AsyncTask<Void, Void, Bitmap> {

        private ContentResolver mContentResolver;
        private Uri mImageUri;
        private int mBitmapWidth;
        private Matrix mCurrentMatrix;
        private IZoomableImageView mListener;
        private Point mScreenSize;

        public AsyncImageRegionDecoder(ContentResolver mContentResolver, Uri mImageUri, int mBitmapWidth,
                                Matrix mCurrentMatrix, IZoomableImageView mListener, Point mScreenSize) {
            this.mContentResolver = mContentResolver;
            this.mImageUri = mImageUri;
            this.mBitmapWidth = mBitmapWidth;
            this.mCurrentMatrix = mCurrentMatrix;
            this.mListener = mListener;
            this.mScreenSize = mScreenSize;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null && !isCancelled() && mListener != null) {
                mListener.setRegionBitmap(bitmap);
            }
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            try {
                InputStream input = mContentResolver.openInputStream(mImageUri);
                BitmapRegionDecoder bitmapRegionDecoder = BitmapRegionDecoder.newInstance(input, false);
                int originalWidth = bitmapRegionDecoder.getWidth();
                float originalSampling = originalWidth / (float) mBitmapWidth;

                float[] matrixValues = new float[9];
                mCurrentMatrix.getValues(matrixValues);
                float offsetX = matrixValues[Matrix.MTRANS_X];
                float offsetY = matrixValues[Matrix.MTRANS_Y];
                float currentScale = matrixValues[Matrix.MSCALE_X];

                int regionWidth = (int) (mScreenSize.x / currentScale * originalSampling);
                int regionHeight = (int) (mScreenSize.y / currentScale * originalSampling);
                int regionStartX = (int) (-offsetX / currentScale * originalSampling);
                int regionStartY = (int) (-offsetY / currentScale * originalSampling);

                Rect region = new Rect(regionStartX, regionStartY,
                    regionStartX + regionWidth, regionStartY + regionHeight);

                if (region.height() > 4096 || region.width() > 4096) {
                    return null;
                }

                Bitmap imageRegion = bitmapRegionDecoder.decodeRegion(region, null);
                imageRegion = Bitmap.createScaledBitmap(imageRegion, mScreenSize.x, mScreenSize.y,
                    false);
                if (input != null) {
                    input.close();
                }

                return imageRegion;
            } catch (IOException e) {
                e.printStackTrace();

                return null;
            }
        }
    }
}