package barsotti.alejandro.prototipotf.photoCapture;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;

import barsotti.alejandro.prototipotf.R;
import barsotti.alejandro.prototipotf.utils.ImageUtils;

public class PhotoCaptureMainActivity extends AppCompatActivity {

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_IMAGE_PICK = 2;

    ImageView imagePreview;
    ConstraintLayout imageOriginOptionsLayout;
    ConstraintLayout imagePreviewLayout;
    Uri imageUri;
    LinearLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_photo_capture_main);

        imagePreview = findViewById(R.id.image_preview);
        imageOriginOptionsLayout = findViewById(R.id.image_origin_options_layout);
        imagePreviewLayout = findViewById(R.id.image_preview_layout);
        mainLayout = findViewById(R.id.photo_capture_main_layout);

        hidePreview();
    }

    public void takePicture(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            Uri photoFileUri = ImageUtils.createImageFile(this);
            if (photoFileUri != null) {
                imageUri = photoFileUri;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFileUri);
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
            ImageUtils.addFileToMediaScannerService(this, imageUri.getPath());
            showPreview();
        } else if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            imageUri = data.getData();
            if (imageUri != null) {
                ImageUtils.addFileToMediaScannerService(this, imageUri.getPath());
            }
            showPreview();
        }
    }

    private void hidePreview() {
        imagePreviewLayout.setVisibility(View.GONE);
        imageOriginOptionsLayout.setVisibility(View.VISIBLE);
        imagePreview.setImageURI(null);
    }

    private void showPreview() {
        // Cargar preview de la imagen seleccionada.
        Glide.with(this)
            .load(imageUri)
            .into(imagePreview);
        imagePreviewLayout.setVisibility(View.VISIBLE);
        imageOriginOptionsLayout.setVisibility(View.GONE);
    }

    public void cancelPictureSelection(View view) {
        hidePreview();
    }

    public void confirmPictureSelection(View view) {
        View cancelButton = this.findViewById(R.id.cancel_button);
        View confirmButton = this.findViewById(R.id.confirm_button);

        cancelButton.setEnabled(false);
        confirmButton.setEnabled(false);

        cancelButton.setEnabled(true);
        confirmButton.setEnabled(true);

        // Crear intent y adjuntar ambas Uris (imagen original e imagen solo-bordes).
        Intent intent = new Intent(this, ImageViewerActivity.class);
        intent.putExtra(ImageViewerActivity.BITMAP_URI_EXTRA, imageUri);
        startActivity(intent);
    }
}
