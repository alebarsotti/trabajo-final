package barsotti.alejandro.trabajoFinal;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import barsotti.alejandro.trabajoFinal.mainActivityList.DataItem;
import barsotti.alejandro.trabajoFinal.mainActivityList.DataListAdapter;
import barsotti.alejandro.trabajoFinal.photoCapture.PhotoCaptureMainActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Trabajo Final";
    RecyclerView mDataListRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

//        Toolbar t = findViewById(R.id.toolbar);
//        setSupportActionBar(t);

        mDataListRecyclerView = findViewById(R.id.data_list);

        // Utilizar Linear Layout Manager.
        mLayoutManager = new LinearLayoutManager(this);
        mDataListRecyclerView.setLayoutManager(mLayoutManager);

        // Generar lista de datos.
        List<DataItem> dataItems = new ArrayList<>();
        dataItems.add(new DataItem("Alejandro Barsotti", "Roque Sáenz Peña 557", "hoy"));
        dataItems.add(new DataItem("Juan Pérez", "Av. Italia 111", "ayer"));
        dataItems.add(new DataItem("José Rodríguez", "Sarmiento 234", "04/02/2018"));
        dataItems.add(new DataItem("Ignacio Sánchez", "Arenales 491", "13/01/2018"));
        dataItems.add(new DataItem("Pedro Silva", "Saavedra 52", "27/12/2017"));
        dataItems.add(new DataItem("Alejandro Barsotti", "Roque Sáenz Peña 557", "hoy"));
        dataItems.add(new DataItem("Juan Pérez", "Av. Italia 111", "ayer"));
        dataItems.add(new DataItem("José Rodríguez", "Sarmiento 234", "04/02/2018"));
        dataItems.add(new DataItem("Ignacio Sánchez", "Arenales 491", "13/01/2018"));
        dataItems.add(new DataItem("Pedro Silva", "Saavedra 52", "27/12/2017"));

        // Especificar adaptador.
        DataListAdapter dataListAdapter = new DataListAdapter(dataItems);
        mDataListRecyclerView.setAdapter(dataListAdapter);
    }

    public void newPhotoCapture(View view) {
        Intent intent = new Intent(this, PhotoCaptureMainActivity.class);
        startActivity(intent);
    }
}
