package barsotti.alejandro.tf.activities;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import barsotti.alejandro.tf.R;
import barsotti.alejandro.tf.utils.ImageUtils;
import barsotti.alejandro.tf.views.Angle;
import barsotti.alejandro.tf.views.CartesianAxes;
import barsotti.alejandro.tf.views.Circumference;
import barsotti.alejandro.tf.views.DifferenceHZ;
import barsotti.alejandro.tf.views.ToothPitch;
import barsotti.alejandro.tf.views.ZoomableImageViewGroup;

public class ImageViewerActivity extends AppCompatActivity {
    private static final String TAG = "ImageViewerActivity";

    public static final String BITMAP_URI_EXTRA = "bitmapUri";
    public static final int OFFSET_BETWEEN_ANIMATIONS_IN_MILLIS = 50;
    public static final long ANIMATION_DURATION_IN_MILLIS = 300L;
    public static final int FAB_ROTATE_ANIMATION_DEGREES = 135;
    private static final int PERMISSION_REQUEST_CODE = 101;

    private final int FLOATING_ACTION_BUTTON_CANCEL_COLOR = Color.rgb(239, 83, 80);

    private Point screenSize = new Point();
    private ZoomableImageViewGroup zoomableImageViewGroup;
    private Uri bitmapUri;
    private FloatingActionButton menuFab;
    private FloatingActionButton circumferenceFab;
    private FloatingActionButton tangentFab;
    private FloatingActionButton angleFab;
    private FloatingActionButton takeScreenshotFab;
    private FloatingActionButton confirmFab;
    private FloatingActionButton toothPitchFab;
    private FloatingActionButton differenceHzFab;
    private ArrayList<FloatingActionButton> floatingActionButtonsInMenu = new ArrayList<>();
    private boolean menuVisible = false;
    private ArrayList<Uri> screenshotsTaken = new ArrayList<>();

    public int floatingActionButtonDefaultColor;
    private Snackbar screenshotSavedSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.activity_image_viewer);

        zoomableImageViewGroup = findViewById(R.id.zoomable_image_view_group);

        menuFab = findViewById(R.id.menu_fab);
        angleFab = findViewById(R.id.angle_fab);
        tangentFab = findViewById(R.id.cartesian_axes_fab);
        circumferenceFab = findViewById(R.id.circumference_fab);
        takeScreenshotFab = findViewById(R.id.take_screenshot_fab);
        confirmFab = findViewById(R.id.confirm_fab);
        toothPitchFab = findViewById(R.id.tooth_pitch_fab);
        differenceHzFab = findViewById(R.id.difference_hz_fab);

        floatingActionButtonsInMenu.add(confirmFab);
        floatingActionButtonsInMenu.add(circumferenceFab);
        floatingActionButtonsInMenu.add(tangentFab);
        floatingActionButtonsInMenu.add(angleFab);
        floatingActionButtonsInMenu.add(toothPitchFab);
        floatingActionButtonsInMenu.add(differenceHzFab);
        floatingActionButtonsInMenu.add(takeScreenshotFab);

        floatingActionButtonDefaultColor = ContextCompat.getColor(this, R.color.colorAccent);

        Intent intent = getIntent();
        bitmapUri = intent.getParcelableExtra(BITMAP_URI_EXTRA);

        setImage();

        setupFloatingActionButtons();

        super.onCreate(savedInstanceState);
    }

    private void setupFloatingActionButtons() {
        setupCircumferenceButton();
        setupTangentButton();
        setupAngleButton();
        setupToothPitchButton();
        setupDifferenceHzButton();
        setupTakeScreenshotButton();
        setupConfirmButton();
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
                    CartesianAxes.class);
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

    private void setupToothPitchButton() {
        toothPitchFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideMenu();

                zoomableImageViewGroup.setZoomableImageViewDrawingInProgress(true,
                    ToothPitch.class);
            }
        });

        toothPitchFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(ImageViewerActivity.this, R.string.draw_tooth_pitch_button_message,
                    Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void setupDifferenceHzButton() {
        differenceHzFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideMenu();

                zoomableImageViewGroup.setZoomableImageViewDrawingInProgress(true,
                    DifferenceHZ.class);
            }
        });

        differenceHzFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(ImageViewerActivity.this, R.string.draw_difference_hz_button_message,
                    Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void setupTakeScreenshotButton() {
        takeScreenshotFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideMenu();

                takeScreenshotOption();
            }
        });

        takeScreenshotFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(ImageViewerActivity.this, R.string.save_image_button_message,
                    Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void takeScreenshotOption() {
        if (checkScreenshotPermission()) {
            Uri screenshotUri = takeScreenshot();

            makeScreenshotShareSnackbar(screenshotUri);
        }
    }

    private boolean checkScreenshotPermission() {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && 
            ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);

            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takeScreenshotOption();
        }
        else {
            Toast.makeText(this, R.string.permissions_denied_message, Toast.LENGTH_LONG).show();
        }
    }

    private void setupConfirmButton() {
        confirmFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideMenu();
                Intent intent = MeasurementDetailsActivity.getIntent(getBaseContext(), screenshotsTaken,
                    zoomableImageViewGroup.getAngleMeasures(), zoomableImageViewGroup.getToothMeasures());
                startActivity(intent);
            }
        });

        confirmFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(ImageViewerActivity.this, R.string.confirm_button_message,
                    Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void makeScreenshotShareSnackbar(Uri screenshotUri) {
        screenshotSavedSnackbar = Snackbar
            .make(zoomableImageViewGroup, R.string.screenshot_saved_message, Snackbar.LENGTH_LONG)
            .setAction(R.string.share_action_text, new ShareScreenshotListener(screenshotUri))
            .setActionTextColor(getResources().getColor(R.color.colorAccent));
        screenshotSavedSnackbar.show();
    }

    public void toggleMenu(View view) {
        if (menuVisible) {
            hideMenu();
        }
        else {
            if (screenshotSavedSnackbar != null && screenshotSavedSnackbar.isShown()) {
                screenshotSavedSnackbar.dismiss();
            }
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
        ((View) fab).setVisibility(View.VISIBLE);
        fab.setClickable(true);
        fab.setEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fab.setFocusable(View.FOCUSABLE);
        }
    }

    private void hideFloatingActionButton(FloatingActionButton fab, int index) {

        ScaleAnimation scaleDownAnimation = getScaleAnimation(index, 1, 0, fab);
        fab.startAnimation(scaleDownAnimation);
        ((View) fab).setVisibility(View.GONE);
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
        Uri uri = ImageUtils.takeAndStoreScreenshot(this, zoomableImageViewGroup);
        screenshotsTaken.add(uri);

        return uri;
    }

    private void playShutterSoundEffect() {
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audio != null && audio.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            MediaActionSound sound = new MediaActionSound();
            sound.play(MediaActionSound.SHUTTER_CLICK);
        }
    }

    private void shareScreenshot(Uri screenshotUri) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
        shareIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_screenshot_message)));
    }

    private void setImage() {
        getWindowManager().getDefaultDisplay().getRealSize(screenSize);

        RequestOptions glideOptions = new RequestOptions().fitCenter();

        Glide.with(this)
            .load(bitmapUri)
            .apply(glideOptions)
            .into(new CustomViewTarget<ZoomableImageViewGroup, Drawable>(zoomableImageViewGroup) {
                @Override
                protected void onResourceCleared(@Nullable Drawable placeholder) {
                    //Do nothing
                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    //Do nothing
                }

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