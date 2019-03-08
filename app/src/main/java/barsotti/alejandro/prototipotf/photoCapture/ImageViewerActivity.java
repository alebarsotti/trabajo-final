package barsotti.alejandro.prototipotf.photoCapture;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;

import barsotti.alejandro.prototipotf.R;
import barsotti.alejandro.prototipotf.customViews.Angle;
import barsotti.alejandro.prototipotf.customViews.Circumference;
import barsotti.alejandro.prototipotf.customViews.Tangent;
import barsotti.alejandro.prototipotf.customViews.ZoomableImageViewGroup;
import barsotti.alejandro.prototipotf.utils.MailUtils;
import barsotti.alejandro.prototipotf.utils.ScreenshotUtils;

public class ImageViewerActivity extends AppCompatActivity {
    private static final String TAG = "ImageViewerActivity";

    public static final String BITMAP_URI_EXTRA = "bitmapUri";
    public static final int OFFSET_BETWEEN_ANIMATIONS_IN_MILLIS = 50;
    public static final long ANIMATION_DURATION_IN_MILLIS = 300L;
    public static final int FAB_ROTATE_ANIMATION_DEGREES = 135;

    private final int FLOATING_ACTION_BUTTON_CANCEL_COLOR = Color.rgb(239, 83, 80);

    private Point screenSize = new Point();
    private ZoomableImageViewGroup zoomableImageViewGroup;
    private Uri bitmapUri;
    private FloatingActionButton menuFab;
    private FloatingActionButton circumferenceFab;
    private FloatingActionButton tangentFab;
    private FloatingActionButton angleFab;
    private FloatingActionButton saveImageFab;
    private ArrayList<FloatingActionButton> floatingActionButtonsInMenu = new ArrayList<>();
    private boolean menuVisible = false;

    public int floatingActionButtonDefaultColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.activity_image_viewer);

        zoomableImageViewGroup = findViewById(R.id.zoomable_image_view_group);

        menuFab = findViewById(R.id.menu_fab);
        angleFab = findViewById(R.id.angle_fab);
        tangentFab = findViewById(R.id.tangent_fab);
        circumferenceFab = findViewById(R.id.circumference_fab);
        saveImageFab = findViewById(R.id.save_image_fab);

        floatingActionButtonsInMenu.add(circumferenceFab);
        floatingActionButtonsInMenu.add(tangentFab);
        floatingActionButtonsInMenu.add(angleFab);
        floatingActionButtonsInMenu.add(saveImageFab);

        floatingActionButtonDefaultColor = ContextCompat.getColor(this, R.color.colorAccent);

        Intent intent = getIntent();
        bitmapUri = intent.getParcelableExtra(BITMAP_URI_EXTRA);

        setImage();

        setupFloatingActionButtons();

        super.onCreate(savedInstanceState);
    }

    private void setupFloatingActionButtons() {
        setupSaveImageButton();
        setupCircumferenceButton();
        setupTangentButton();
        setupAngleButton();
    }

    private void setupCircumferenceButton() {
        circumferenceFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideMenu();
                zoomableImageViewGroup.setZoomableImageViewDrawingInProgress(true,
                    Circumference.class);
            }
        });

        circumferenceFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(ImageViewerActivity.this, R.string.draw_circumference_button_message,
                    Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void setupTangentButton() {
        tangentFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideMenu();
                zoomableImageViewGroup.setZoomableImageViewDrawingInProgress(true,
                    Tangent.class);
            }
        });

        tangentFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(ImageViewerActivity.this, R.string.draw_tangent_button_message,
                    Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void setupAngleButton() {
        angleFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideMenu();
                zoomableImageViewGroup.setZoomableImageViewDrawingInProgress(true,
                    Angle.class);
            }
        });

        angleFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(ImageViewerActivity.this, R.string.draw_angle_button_message,
                    Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void setupSaveImageButton() {
        saveImageFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideMenu();
                Uri screenshotUri = takeScreenshot();

                makeScreenshotShareSnackbar(screenshotUri);
            }
        });

        saveImageFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(ImageViewerActivity.this, R.string.save_image_button_message,
                    Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void makeScreenshotShareSnackbar(Uri screenshotUri) {
        Snackbar screenshotSavedSnackbar = Snackbar.make(zoomableImageViewGroup,
            R.string.screenshot_saved_message, Snackbar.LENGTH_LONG);
        screenshotSavedSnackbar.setAction(R.string.share_screenshot_action_text,
            new ShareScreenshotListener(screenshotUri));
        screenshotSavedSnackbar.show();
    }

    public void toggleMenu(View view) {
        if (menuVisible) {
            hideMenu();
        }
        else {
            showMenu();
        }
    }

    private void showMenu() {
        animateFloatingActionButtonRotation(0, FAB_ROTATE_ANIMATION_DEGREES);

        animateFloatingActionButtonColorChange(floatingActionButtonDefaultColor,
            FLOATING_ACTION_BUTTON_CANCEL_COLOR);

        int index = 0;
        for (FloatingActionButton fab: floatingActionButtonsInMenu) {
            showFloatingActionButton(fab, index++);
        }

        menuVisible = true;
    }

    private void animateFloatingActionButtonRotation(int fromDegrees, int toDegrees) {
        RotateAnimation rotateForwardAnimation = new RotateAnimation(fromDegrees, toDegrees,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateForwardAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        rotateForwardAnimation.setDuration(ANIMATION_DURATION_IN_MILLIS);
        rotateForwardAnimation.setFillAfter(true);
        menuFab.startAnimation(rotateForwardAnimation);
    }

    private void animateFloatingActionButtonColorChange(int fromColor, int toColor) {
        ValueAnimator valueAnimator = ValueAnimator
            .ofArgb(fromColor, toColor);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.setDuration(ANIMATION_DURATION_IN_MILLIS);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int animatedValue = (int) valueAnimator.getAnimatedValue();
                menuFab.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
            }
        });
        valueAnimator.start();
    }

    private void hideMenu() {
        animateFloatingActionButtonRotation(FAB_ROTATE_ANIMATION_DEGREES, 0);

        animateFloatingActionButtonColorChange(FLOATING_ACTION_BUTTON_CANCEL_COLOR,
            floatingActionButtonDefaultColor);

        for (int i = floatingActionButtonsInMenu.size() - 1; i >= 0; i--) {
            hideFloatingActionButton(floatingActionButtonsInMenu.get(i),
                floatingActionButtonsInMenu.size() - 1 - i);
        }

        menuVisible = false;
    }

    private void showFloatingActionButton(FloatingActionButton fab, int index) {

        ScaleAnimation scaleUpAnimation = getScaleAnimation(index, 0, 1, fab);
        fab.startAnimation(scaleUpAnimation);
        fab.setVisibility(View.VISIBLE);
        fab.setClickable(true);
        fab.setEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fab.setFocusable(View.FOCUSABLE);
        }
    }

    private void hideFloatingActionButton(FloatingActionButton fab, int index) {

        ScaleAnimation scaleDownAnimation = getScaleAnimation(index, 1, 0, fab);
        fab.startAnimation(scaleDownAnimation);
        fab.setVisibility(View.GONE);
        fab.setClickable(false);
        fab.setEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fab.setFocusable(View.NOT_FOCUSABLE);
        }
    }

    @NonNull
    private ScaleAnimation getScaleAnimation(int animationOrderIndex, int fromScale, int toScale,
                                             final FloatingActionButton floatingActionButton) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(fromScale, toScale, fromScale, toScale,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setDuration(ANIMATION_DURATION_IN_MILLIS);
        scaleAnimation.setStartOffset(animationOrderIndex * OFFSET_BETWEEN_ANIMATIONS_IN_MILLIS);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                floatingActionButton.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        return scaleAnimation;
    }

    private Uri takeScreenshot() {
        playShutterSoundEffect();

        return ScreenshotUtils.takeAndStoreScreenshot(this, zoomableImageViewGroup);
    }

    private void playShutterSoundEffect() {
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audio != null) {
            switch(audio.getRingerMode()){
                case AudioManager.RINGER_MODE_NORMAL:
                    MediaActionSound sound = new MediaActionSound();
                    sound.play(MediaActionSound.SHUTTER_CLICK);
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    break;
            }
        }
    }

    private void shareScreenshot(Uri screenshotUri) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
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

    private void sendEmailWithScreenshot(Uri screenshotUri) {
        String[] addresses = {};
        String subject = getString(R.string.default_mail_with_attachment_subject);
        Intent mailIntent = MailUtils.composeEmailWithAttachment(addresses, subject, screenshotUri);
        if (mailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mailIntent);
        }
        else {
            Log.e(TAG, "sendEmail: " + getString(R.string.sendEmail_error_message));
        }
    }

    private void setImage() {
        getWindowManager().getDefaultDisplay().getRealSize(screenSize);

        RequestOptions glideOptions = new RequestOptions()
            .fitCenter()
            .override(screenSize.x, screenSize.y);

        Glide.with(this)
            .load(bitmapUri)
            .apply(glideOptions)
            .into(new ViewTarget<ZoomableImageViewGroup, Drawable>(zoomableImageViewGroup) {
                @Override
                public void onResourceReady(@NonNull Drawable resource,
                                            @Nullable Transition<? super Drawable> transition) {
                    zoomableImageViewGroup.setupZoomableImageView(screenSize.x, screenSize.y,
                        bitmapUri, resource);
                }
            });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        View decorView = getWindow().getDecorView();
        if (hasFocus) {
            decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
        }
    }

    private class ShareScreenshotListener implements View.OnClickListener {
        private Uri screenshotUri;

        ShareScreenshotListener(Uri screenshotUri) {
            this.screenshotUri = screenshotUri;
        }

        @Override
        public void onClick(View view) {
            shareScreenshot(screenshotUri);
        }
    }
}