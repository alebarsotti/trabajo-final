package barsotti.alejandro.tf.activities;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ShareCompat;
import barsotti.alejandro.tf.R;
import barsotti.alejandro.tf.interfaces.MeasurementDetailsView;
import barsotti.alejandro.tf.presenters.MeasurementDetailsPresenter;
import barsotti.alejandro.tf.utils.MailUtils;

public class MeasurementDetailsActivity extends AppCompatActivity implements MeasurementDetailsView {
    public static final String NEWLINE_CHAR = "\n";
    public static final String SEPARATOR = ": ";
    public static final String EMPTY_FORM_FIELD_TEXT = "-";
    public static final String EMPTY_STRING = "";
    private static final String TAG = MeasurementDetailsActivity.class.getName();
    private static final String SCREENSHOTS_TAKEN_EXTRA = "screenshots_taken";
    private static final String ANGLE_MEASURES_EXTRA = "angle_measures";
    private static final String TOOTH_MEASURES_EXTRA = "tooth_measures";
    public static final String MEASUREMENT_INFO_CLIPBOARD_LABEL = "measurement info";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy - hh:mm",
        Locale.getDefault());

    private ArrayList<Uri> screenshotsTaken;
    private ViewGroup rootViewGroup;
    private String angleMeasures;
    private String toothMeasures;
    private MeasurementDetailsPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement_details);

        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        screenshotsTaken = intent.getParcelableArrayListExtra(SCREENSHOTS_TAKEN_EXTRA);
        angleMeasures = intent.getStringExtra(ANGLE_MEASURES_EXTRA);
        toothMeasures = intent.getStringExtra(TOOTH_MEASURES_EXTRA);

        rootViewGroup = findViewById(R.id.root);

        presenter = MeasurementDetailsPresenter.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();

        restoreState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.measurement_details, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.clear_fields:
                clearFields();
                return true;
            case R.id.content_copy:
                copyFieldsTextToClipboard();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void copyFieldsTextToClipboard() {
        try {
            String measurementInfoSummary = buildMeasurementInfoSummary();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(MEASUREMENT_INFO_CLIPBOARD_LABEL, measurementInfoSummary);
            clipboard.setPrimaryClip(clip);

            Snackbar shareClipboardSnackbar = Snackbar.make(findViewById(R.id.measurement_details_layout),
                R.string.copy_fields_toast_message, Snackbar.LENGTH_LONG);
            shareClipboardSnackbar.setAction(R.string.share_action_text,
                new ShareMeasurementDetailsListener(measurementInfoSummary, this));
            shareClipboardSnackbar.show();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.copy_fields_toast_error_message),
                Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void clearFields() {
        for (int i = 0; i < rootViewGroup.getChildCount(); i++) {
            View child = rootViewGroup.getChildAt(i);
            if (child instanceof EditText) {
                ((EditText) child).setText(EMPTY_STRING);
            }
        }
    }

    public static Intent getIntent(Context context, ArrayList<Uri> screenshotsTaken, String angleMeasures,
                                   String toothMeasures) {
        Intent intent = new Intent(context, MeasurementDetailsActivity.class);
        intent.putParcelableArrayListExtra(SCREENSHOTS_TAKEN_EXTRA, screenshotsTaken);
        intent.putExtra(ANGLE_MEASURES_EXTRA, angleMeasures);
        intent.putExtra(TOOTH_MEASURES_EXTRA, toothMeasures);

        return intent;
    }

    public void goBack(View view) {
        saveState();
        finish();
    }

    public void sendEmail(View view) {
        String subject = String.format(getString(R.string.default_mail_with_attachment_subject),
            dateFormat.format(new Date()));
        String body = buildMeasurementInfoSummary();
        Intent mailIntent = MailUtils.composeEmailWithMultipleAttachments(subject, body, screenshotsTaken);
        if (mailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mailIntent);
        } else {
            Log.e(TAG, "sendEmail: " + getString(R.string.sendEmail_error_message));
        }
    }

    private String buildMeasurementInfoSummary() {
        StringBuilder bodyBuilder = new StringBuilder();
        for (int i = 0; i < rootViewGroup.getChildCount(); i++) {
            View child = rootViewGroup.getChildAt(i);
            if (child instanceof EditText) {
                CharSequence hint = ((EditText) child).getHint();
                String text = ((EditText) child).getText().toString();
                bodyBuilder.append(hint)
                    .append(SEPARATOR)
                    .append(!text.isEmpty() ? text : EMPTY_FORM_FIELD_TEXT)
                    .append(NEWLINE_CHAR);
            }
        }

        if (angleMeasures != null && !angleMeasures.isEmpty()) {
            bodyBuilder.append(NEWLINE_CHAR)
                .append(angleMeasures)
                .append(NEWLINE_CHAR);
        }

        if (toothMeasures != null && !toothMeasures.isEmpty()) {
            bodyBuilder.append(toothMeasures);
        }

        return bodyBuilder.toString();
    }

    public void goBackToRoot(View view) {
        Intent intent = PhotoCaptureActivity.getIntent(this);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        saveState();
        super.onBackPressed();
    }

    private void saveState() {
        presenter.saveState(buildStateBundle());
    }

    private void restoreState() {
        Bundle state = presenter.getState();
        if (state != null) {
            restoreFormState(state);
        }
        presenter.clearState();
    }

    private Bundle buildStateBundle() {
        Bundle bundle = new Bundle();
        for (int i = 0; i < rootViewGroup.getChildCount(); i++) {
            View child = rootViewGroup.getChildAt(i);
            if (child instanceof EditText) {
                String hint = ((EditText) child).getHint().toString();
                String text = ((EditText) child).getText().toString();
                bundle.putString(hint, text);
            }
        }

        return bundle;
    }

    @Override
    public void restoreFormState(Bundle bundle) {
        for (int i = 0; i < rootViewGroup.getChildCount(); i++) {
            View child = rootViewGroup.getChildAt(i);
            if (child instanceof EditText) {
                String hint = ((EditText) child).getHint().toString();
                String text = bundle.getString(hint, EMPTY_STRING);
                ((EditText) child).setText(text);
            }
        }
    }

    public class ShareMeasurementDetailsListener implements View.OnClickListener {
        private String measurementDetails;
        private Activity context;

        ShareMeasurementDetailsListener(String measurementDetails, Activity context) {
            this.measurementDetails = measurementDetails;
            this.context = context;
        }

        @Override
        public void onClick(View view) {
            ShareCompat.IntentBuilder
                .from(context)
                .setText(measurementDetails)
                .setType("text/plain")
                .setChooserTitle(R.string.share_measurement_details_message)
                .startChooser();
        }
    }
}
