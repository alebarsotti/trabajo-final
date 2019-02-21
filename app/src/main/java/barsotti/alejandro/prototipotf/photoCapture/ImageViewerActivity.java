package barsotti.alejandro.prototipotf.photoCapture;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;

import barsotti.alejandro.prototipotf.R;
import barsotti.alejandro.prototipotf.customViews.Angle;
import barsotti.alejandro.prototipotf.customViews.Circumference;
import barsotti.alejandro.prototipotf.customViews.Tangent;
import barsotti.alejandro.prototipotf.customViews.ZoomableImageViewGroup;
import barsotti.alejandro.prototipotf.utils.MailUtils;
import barsotti.alejandro.prototipotf.utils.ScreenshotUtils;

public class ImageViewerActivity extends AppCompatActivity {
    public static final String BITMAP_URI_EXTRA = "bitmapUri";
    private static final String TAG = "ImageViewerActivity";

    private Point mScreenSize = new Point();
    private DrawerLayout mDrawerLayout;
    private ZoomableImageViewGroup mZoomableImageViewGroup;
    private Uri mBitmapUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.activity_image_viewer);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView mNavigationView = findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(new ItemSelectedLister());

        mZoomableImageViewGroup = findViewById(R.id.zoomable_image_view_group);

        Intent intent = getIntent();
        mBitmapUri = intent.getParcelableExtra(BITMAP_URI_EXTRA);

        setBitmap();

        super.onCreate(savedInstanceState);
    }

    private class ItemSelectedLister implements NavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            mDrawerLayout.closeDrawers();

            switch (item.getItemId()) {
                case R.id.draw_circumference: {
                    mZoomableImageViewGroup.setZoomableImageViewDrawingInProgress(true,
                        Circumference.class);
                    break;
                }
                case R.id.draw_tangent: {
                    mZoomableImageViewGroup.setZoomableImageViewDrawingInProgress(true,
                        Tangent.class);
                    break;
                }
                case R.id.draw_angle: {
                    mZoomableImageViewGroup.setZoomableImageViewDrawingInProgress(true,
                        Angle.class);
                    break;
                }
                case R.id.share_screenshot: {
                    shareScreenshot();
                    break;
                }
                case R.id.send_email: {
                    sendEmail();
                    break;
                }
                case R.id.send_email_with_screenshot: {
                    sendEmailWithScreenshot();
                    break;
                }
            }

            return true;
        }
    }

    private void shareScreenshot() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        File screenshotFile = ScreenshotUtils.takeAndStoreScreenshot(mZoomableImageViewGroup);
        Uri screenshotUri = Uri.fromFile(screenshotFile);
        shareIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
        shareIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_screenshot_message)));
    }

    private void sendEmail() {
        String[] addresses = {};
        String subject = getString(R.string.default_mail_subject);
        Intent mailIntent = MailUtils.composeEmail(addresses, subject);
        if (mailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mailIntent);
        }
        else {
            Log.e(TAG, "sendEmail: " + getString(R.string.sendEmail_error_message));
        }
    }

    private void sendEmailWithScreenshot() {
        String[] addresses = {};
        String subject = getString(R.string.default_mail_with_attachment_subject);
        File screenshotFile = ScreenshotUtils.takeAndStoreScreenshot(mZoomableImageViewGroup);
        Uri screenshotUri = Uri.fromFile(screenshotFile);
        Intent mailIntent = MailUtils.composeEmailWithAttachment(addresses, subject, screenshotUri);
        if (mailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mailIntent);
        }
        else {
            Log.e(TAG, "sendEmail: " + getString(R.string.sendEmail_error_message));
        }
    }

    private void setBitmap() {
        getWindowManager().getDefaultDisplay().getRealSize(mScreenSize);

        RequestOptions glideOptions = new RequestOptions()
            .fitCenter()
            .override(mScreenSize.x, mScreenSize.y);

        Glide.with(this)
            .load(mBitmapUri)
            .apply(glideOptions)
            .into(new ViewTarget<ZoomableImageViewGroup, Drawable>(mZoomableImageViewGroup) {
                @Override
                public void onResourceReady(@NonNull Drawable resource,
                                            @Nullable Transition<? super Drawable> transition) {
                    mZoomableImageViewGroup.setupZoomableImageView(mScreenSize.x, mScreenSize.y,
                        mBitmapUri, resource);
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