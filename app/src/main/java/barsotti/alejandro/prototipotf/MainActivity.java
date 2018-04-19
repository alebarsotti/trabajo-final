package barsotti.alejandro.prototipotf;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import barsotti.alejandro.prototipotf.mainActivityList.DataItem;
import barsotti.alejandro.prototipotf.mainActivityList.DataListAdapter;
import barsotti.alejandro.prototipotf.photoCapture.PhotoCaptureMainActivity;

public class MainActivity extends AppCompatActivity {
    RecyclerView mDataListRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDataListRecyclerView = findViewById(R.id.data_list);

        // Use a Linear Layout Manager.
        mLayoutManager = new LinearLayoutManager(this);
        mDataListRecyclerView.setLayoutManager(mLayoutManager);

        // Generate the data list.
        List<DataItem> dataItems = new ArrayList<>();
        dataItems.add(new DataItem("Alejandro Barsotti", "Roque Sáenz Peña 557", "hoy"));
        dataItems.add(new DataItem("Juan Pérez", "Av. Italia 111", "ayer"));
        dataItems.add(new DataItem("José Rodríguez", "Sarmiento 234", "04/02/2018"));
        dataItems.add(new DataItem("Ignacio Sánchez", "Arenales 491", "13/01/2018"));
        dataItems.add(new DataItem("Pedro Silva", "Saavedra 52", "27/12/2017"));

        // Specify an adapter.
        DataListAdapter dataListAdapter = new DataListAdapter(dataItems);
        mDataListRecyclerView.setAdapter(dataListAdapter);
    }

    public void newPhotoCapture(View view) {
        Intent intent = new Intent(this, PhotoCaptureMainActivity.class);
        startActivity(intent);
    }
}
