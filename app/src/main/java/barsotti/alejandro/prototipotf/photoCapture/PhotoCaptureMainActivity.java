package barsotti.alejandro.prototipotf.photoCapture;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;

import java.io.File;

import barsotti.alejandro.prototipotf.R;
import barsotti.alejandro.prototipotf.utils.ContentProvidersUtils;

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

        this.findViewById(R.id.cancel_action).setEnabled(false);
        this.findViewById(R.id.confirm_action).setEnabled(false);

        this.findViewById(R.id.cancel_action).setEnabled(true);
        this.findViewById(R.id.confirm_action).setEnabled(true);

        // Crear intent y adjuntar ambas Uris (imagen original e imagen solo-bordes).
        Intent intent = new Intent(this, ImageViewerActivity.class);
        intent.putExtra(ImageViewerActivity.BITMAP_URI_EXTRA, mImageUri);
        startActivity(intent);
    }
}
