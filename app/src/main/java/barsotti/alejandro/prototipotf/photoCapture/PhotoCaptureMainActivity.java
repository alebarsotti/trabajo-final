package barsotti.alejandro.prototipotf.photoCapture;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import barsotti.alejandro.prototipotf.R;
import barsotti.alejandro.prototipotf.Utils.ContentProvidersUtils;

public class PhotoCaptureMainActivity extends AppCompatActivity {

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_IMAGE_PICK = 2;
    private static final String EDGES_ONLY_SUFFIX = "_edges";

    ImageView mImagePreview;
    ConstraintLayout mImageOriginOptionsLayout;
    ConstraintLayout mImagePreviewLayout;
    Uri mImageUri;
    String mImageFilename;
    String mImageFilepath;
    ConstraintLayout mProgressBarLayout;
    TextView mProgressText;
    LinearLayout mMainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_photo_capture_main);

        mImagePreview = findViewById(R.id.image_preview);
        mImageOriginOptionsLayout = findViewById(R.id.image_origin_options_layout);
        mImagePreviewLayout = findViewById(R.id.image_preview_layout);
        mProgressBarLayout = findViewById(R.id.progress_bar_layout);
        mProgressText = findViewById(R.id.progress_text);
        mMainLayout = findViewById(R.id.photo_capture_main_layout);

        hidePreview();
    }

    public void takePicture(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = ContentProvidersUtils.createImageFile(this);
            if (photoFile != null) {
                mImageFilepath = photoFile.getAbsolutePath();
                Uri fileUri = FileProvider.getUriForFile(this,
                    getString(R.string.file_provider_authority), photoFile);
                mImageUri = fileUri;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    public void pickPicture(View view) {
        Intent intent = new Intent();
        // Solo mostrar archivos de imágenes.
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            mImageFilename = ContentProvidersUtils.getFilenameFromUri(this, mImageUri);
            showPreview();
        } else if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            mImageUri = data.getData();
            mImageFilename = ContentProvidersUtils.getFilenameFromUri(this, mImageUri);
            mImageFilepath = ContentProvidersUtils.getPathFromUri(this, mImageUri);
            showPreview();
        }
    }

    private void hidePreview() {
        mImagePreviewLayout.setVisibility(View.GONE);
        mImageOriginOptionsLayout.setVisibility(View.VISIBLE);
        mImagePreview.setImageURI(null);
    }

    private void showPreview() {
        // Cargar preview de la imagen seleccionada.
        Glide.with(this)
            .load(mImageUri)
            .into(mImagePreview);
        mImagePreviewLayout.setVisibility(View.VISIBLE);
        mImageOriginOptionsLayout.setVisibility(View.GONE);
    }

    public void cancelPictureSelection(View view) {
        hidePreview();
    }

    public void confirmPictureSelection(View view) {
        CreateEdgesOnlyBitmapTask createEdgesOnlyBitmapTask = new CreateEdgesOnlyBitmapTask(this);
        createEdgesOnlyBitmapTask.execute();
    }

    private static class CreateEdgesOnlyBitmapTask extends AsyncTask<Void, Void, Uri> {

        private static final long ANIMATION_DURATION = 1500;
        private WeakReference<PhotoCaptureMainActivity> mActivity;

        CreateEdgesOnlyBitmapTask(PhotoCaptureMainActivity activity) {
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            PhotoCaptureMainActivity activity = mActivity.get();
            activity.findViewById(R.id.cancel_action).setEnabled(false);
            activity.findViewById(R.id.confirm_action).setEnabled(false);
            activity.mProgressBarLayout.setVisibility(View.VISIBLE);
            activity.mProgressText.setText(R.string.detect_edges_progress_text);

            // Animación del texto.
            TranslateAnimation translateAnimation = new TranslateAnimation(
                Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                Animation.RELATIVE_TO_SELF, -0.5f, Animation.RELATIVE_TO_SELF, 0);
            translateAnimation.setDuration(ANIMATION_DURATION);

            AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
            alphaAnimation.setDuration(ANIMATION_DURATION);

            AnimationSet animationSet = new AnimationSet(true);
            animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animationSet.addAnimation(translateAnimation);
            animationSet.addAnimation(alphaAnimation);

            activity.mProgressText.startAnimation(animationSet);
        }

        @Override
        protected void onPostExecute(Uri uri) {
            PhotoCaptureMainActivity activity = mActivity.get();
            activity.mProgressBarLayout.setVisibility(View.GONE);
            activity.findViewById(R.id.cancel_action).setEnabled(true);
            activity.findViewById(R.id.confirm_action).setEnabled(true);

            // Crear intent y adjuntar ambas Uris (imagen original e imagen solo-bordes).
            Intent intent = new Intent(activity, ImageViewerActivity.class);
            intent.putExtra(ImageViewerActivity.BITMAP_URI_EXTRA, activity.mImageUri);
            intent.putExtra(ImageViewerActivity.BITMAP_EDGES_URI_EXTRA, uri);
            activity.startActivity(intent);
            mActivity.clear();
        }

        @Override
        protected Uri doInBackground(Void... voids) {
            PhotoCaptureMainActivity activity = mActivity.get();

            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat =
                new SimpleDateFormat(activity.getString(R.string.photo_file_name_date_format));
            Date dateTime = Calendar.getInstance().getTime();
            String currentDateTime = dateFormat.format(dateTime);

            int dotIndex = activity.mImageFilename.lastIndexOf('.');
            String edgesOnlyFilename = activity.mImageFilename.substring(0, dotIndex) + currentDateTime +
                EDGES_ONLY_SUFFIX + activity.mImageFilename.substring(dotIndex);

            return ImageProcessingUtils.detectEdges(activity.mImageFilepath, edgesOnlyFilename, activity);
        }
    }
}
