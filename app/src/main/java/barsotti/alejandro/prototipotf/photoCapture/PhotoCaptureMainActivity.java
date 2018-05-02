package barsotti.alejandro.prototipotf.photoCapture;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import barsotti.alejandro.prototipotf.R;

public class PhotoCaptureMainActivity extends AppCompatActivity {

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_IMAGE_PICK = 2;
    private static final String EDGES_ONLY_SUFFIX = "_edges";

    ImageView mImagePreview;
    ConstraintLayout mImageOriginOptionsLayout;
    ConstraintLayout mImagePreviewLayout;
    Uri mImageUri;
    Uri mImageEdgesUri;
    String mImageFilename;

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

        hidePreview();
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
            showPreview();
        }
        else if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            mImageUri = data.getData();
            mImageFilename = getFilenameFromUri();
            showPreview();
        }
    }

    private Uri createEdgesOnlyBitmap() {
        // Generar nombre de la imagen resultante.
        int dotIndex = mImageFilename.lastIndexOf('.');
        String edgesOnlyFilename =
            mImageFilename.substring(0, dotIndex) + EDGES_ONLY_SUFFIX + mImageFilename.substring(dotIndex);

        // Obtener el directorio de salida para la imagen.
        File outputDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // File outputDirectory = getExternalFilesDir(Environment.DIRECTORY_DCIM);

        // Crear archivo.
        File edgesBitmap = new File(outputDirectory, edgesOnlyFilename);

        try {
            // Crear output stream.
            FileOutputStream fileOutputStream = new FileOutputStream(edgesBitmap);

            // Obtener imagen original.
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);

            // Generar imagen solo-bordes.
            bitmap = ImageProcessingUtils.detectEdges(bitmap);

            // Guardar nueva imagen.
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            // Agregar a MediaStore.
            ContentResolver cr = getContentResolver();
            String imagePath = edgesBitmap.getAbsolutePath();
            String name = edgesBitmap.getName();
            String description = "Edges Only Image";
            MediaStore.Images.Media.insertImage(cr, imagePath, name, description);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Devolver Uri de la imagen solo-bordes.
        return Uri.fromFile(edgesBitmap);
    }

    private String getFilenameFromUri() {
        ContentResolver contentResolver = getContentResolver();
        Cursor returnCursor =
            contentResolver.query(mImageUri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();

        return name;
    }

    private void hidePreview() {
        mImagePreviewLayout.setVisibility(View.GONE);
        mImageOriginOptionsLayout.setVisibility(View.VISIBLE);
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
        // TODO: Crear imagen solo-bordes asíncronamente.
        Uri edgesOnlyBitmapUri = createEdgesOnlyBitmap();

        // Crear intent y adjuntar ambas Uris (imagen original e imagen solo-bordes).
        Intent intent = new Intent(this, ImageViewerActivity.class);
        intent.putExtra(ImageViewerActivity.BITMAP_URI_EXTRA, mImageUri);
        intent.putExtra(ImageViewerActivity.BITMAP_EDGES_URI_EXTRA, edgesOnlyBitmapUri);
        startActivity(intent);
    }

    private File createImageFile() throws IOException {
        // Crear un nombre único para el archivo de imagen.
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat =
            new SimpleDateFormat(getString(R.string.photo_file_name_date_format));
        Date dateTime = Calendar.getInstance().getTime();
        String photoFileName = getText(R.string.app_name) + dateFormat.format(dateTime);

        // Obtener el directorio de salida para la imagen.
        File outputDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Crear y devolver el archivo de imagen.
        return File.createTempFile(photoFileName, getString(R.string.photo_file_format), outputDirectory);
    }
}
