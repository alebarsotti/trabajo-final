package barsotti.alejandro.tf.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import barsotti.alejandro.tf.R;
import android.os.Bundle;

public class MeasurementDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement_details);

        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);
    }
}
