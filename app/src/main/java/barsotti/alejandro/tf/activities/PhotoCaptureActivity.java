package barsotti.alejandro.tf.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import barsotti.alejandro.tf.R;
import barsotti.alejandro.tf.utils.ImageUtils;

public class PhotoCaptureActivity extends AppCompatActivity {

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_IMAGE_PICK = 2;
    private static final int PERMISSIONS_REQUEST_CODE = 100;

    String[] permissionsRequired = new String[] {
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    ImageView imagePreview;
    ConstraintLayout imageOriginOptionsLayout;
    ConstraintLayout imagePreviewLayout;
    Uri imageUri;
    LinearLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_capture_main);

        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);

        imagePreview = findViewById(R.id.image_preview);
        imageOriginOptionsLayout = findViewById(R.id.image_origin_options_layout);
        imagePreviewLayout = findViewById(R.id.image_preview_layout);
        mainLayout = findViewById(R.id.photo_capture_main_layout);

        findViewById(R.id.take_new_picture_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestTakePicture(view);
            }
        });
        findViewById(R.id.select_image_from_device_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickPicture(view);
            }
        });

        hidePreview();
    }

    public void requestTakePicture(View view) {
        requestPermissionsIfNotGranted();
    }

    public void takePicture() {
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

    private void requestPermissionsIfNotGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !checkMultiplePermissions(this, permissionsRequired)) {
                ActivityCompat.requestPermissions(this, permissionsRequired, PERMISSIONS_REQUEST_CODE);
        }
        else {
            takePicture();
        }
    }

    private boolean checkMultiplePermissions(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean permissionsGranted = true;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    permissionsGranted = false;
                }
            }
            if (permissionsGranted) {
                takePicture();
            }
            else {
                Toast.makeText(this, R.string.permissions_denied_message,
                    Toast.LENGTH_LONG).show();
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
        // Crear intent y adjuntar Uri.
        Intent intent = new Intent(this, ImageViewerActivity.class);
        intent.putExtra(ImageViewerActivity.BITMAP_URI_EXTRA, imageUri);
        startActivity(intent);
    }
}
