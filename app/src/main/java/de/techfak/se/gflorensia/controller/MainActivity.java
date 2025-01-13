package de.techfak.se.gflorensia.controller;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

import androidx.appcompat.app.AlertDialog;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.content.Intent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import de.techfak.se.gflorensia.NoMapSelectedException;
import de.techfak.se.gflorensia.R;
import de.techfak.se.gflorensia.model.PointOfInterest;

/**
 * This is the MainActivity that is executed when the app is started.
 */
public class MainActivity extends BaseActivity {

    String selectedMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager assetManager = getAssets();
        String path = "maps";
        Map<String, PointOfInterest> poiMap = new HashMap<>();
        Spinner spinnerMap = findViewById(R.id.spinner4);
        ArrayList<String> mapNames = new ArrayList<>();
        mapNames.add("Select an option");
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("You haven't chosen a map!");

        List<String> files = getFolder(path);
        if (files != null && !files.isEmpty()) {
            boolean geoJsonFound = false;
            // Loop through the files and filter .geojson files
            for (String file : files) {
                if (file.endsWith(".geojson")) {
                    mapNames.add(getFileNameWithoutExtension(file));
                }
            }
        }

        /* Call the spinner */
        ArrayAdapter<String> adapter = new ArrayAdapter(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                mapNames.toArray()
        );
        spinnerMap.setAdapter(adapter);

        spinnerMap.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMap = spinnerMap.getSelectedItem().toString();
                Log.i("Element gewählt", "Ein Element wurde ausgewählt " + selectedMap);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                AlertDialog noMapSelected = builder.create();
                noMapSelected.show();
                try {
                    throw new NoMapSelectedException();
                } catch (NoMapSelectedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static String getFileNameWithoutExtension(String filename) {
        File file = new File(filename);
        String name = file.getName();
        int lastDotIndex = name.lastIndexOf(".");

        // Remove the extension
        String nameWithoutExtension;
        if (lastDotIndex == -1) {
            nameWithoutExtension = name;
        } else {
            nameWithoutExtension = name.substring(0, lastDotIndex);
        }
        return nameWithoutExtension;
    }

    public void onClick(View view) {
        Intent intent = new Intent(this, StartActivity.class);
        intent.putExtra("chosen_map", selectedMap);
        startActivity(intent);
    }

}
