package barsotti.alejandro.prototipotf.photoCapture;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import barsotti.alejandro.prototipotf.R;

public class PhotoCaptureMainActivity extends AppCompatActivity {

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_IMAGE_PICK = 2;

    Bitmap mBitmap;
    ImageView mImagePreview;
    ConstraintLayout mImageOriginOptionsLayout;
    ConstraintLayout mImagePreviewLayout;
    Uri mImageUri;

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

        togglePreview();
    }

    public void takePicture(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                Uri fileUri = FileProvider.getUriForFile(this, getString(R.string.file_provider_authority),
                    photoFile);
                mImageUri = fileUri;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    public void pickPicture(View view) {
        Intent intent = new Intent();
        // Only show image files.
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Show the chooser (if there are multiple options available).
            startActivityForResult(Intent.createChooser(intent, getString(R.string.pick_image_chooser_title)), REQUEST_IMAGE_PICK);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap image = null;
            try {
                image = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
                togglePreview(true, image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            Uri uri = data.getData();

            try {
                mImageUri = uri;
                Bitmap image = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
                mBitmap = image;
                togglePreview(true, image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void togglePreview() {
        togglePreview(false, null);
    }

    private void togglePreview(boolean show, Bitmap image) {
        if (show) {
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int widthPixels = displayMetrics.widthPixels;
            float scale = (float) widthPixels / image.getWidth();
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(image, widthPixels,
                (int) (scale * image.getHeight()), false);
            mImagePreview.setImageBitmap(scaledBitmap);
            mImagePreviewLayout.setVisibility(View.VISIBLE);
            mImageOriginOptionsLayout.setVisibility(View.GONE);
        }
        else {
            mImagePreviewLayout.setVisibility(View.GONE);
            mImageOriginOptionsLayout.setVisibility(View.VISIBLE);
        }
    }

    public void cancelPictureSelection(View view) {
        togglePreview();
    }

    public void confirmPictureSelection(View view) {
        Intent intent = new Intent(this, ImageViewerActivity.class);
        intent.putExtra(ImageViewerActivity.BITMAP_URI_EXTRA, mImageUri);
        startActivity(intent);
    }

    private File createImageFile() throws IOException {
        // Create a unique name for the photo file.
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.photo_file_name_date_format));
        Date dateTime = Calendar.getInstance().getTime();
        String photoFileName = getText(R.string.app_name) + dateFormat.format(dateTime);

        // Get the output directory for the photo.
        File outputDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Create and return the image file.
        return File.createTempFile(photoFileName, getString(R.string.photo_file_format), outputDirectory);
    }
}
