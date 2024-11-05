package de.techfak.se.gflorensia;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.content.Intent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.json.JSONException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
/**
 * This is the MainActivity that is executed when the app is started.
 */
public class MainActivity extends BaseActivity {
    String TAG = "GeoJson";
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
                    Log.d(TAG, "GeoJson file found: " + file);
                    geoJsonFound = true;
                    String jsonContent = getJsonContent(path + "/" + file);
                    try {
                        poiMap = extractPOI(jsonContent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //Add the file name without geojson as a possible map name
                    mapNames.add(getFileNameWithoutExtension(file));
                }
                if (file.equals("small.geojson")) {
                    String smallJson = getJsonContent(path + "/" + "small.geojson");
                    try {
                        createConnections(smallJson, poiMap);
                    } catch (IOException | JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (file.equals("corrupted.geojson")){
                    String corruptedJson = getJsonContent(path + "/" + "corrupted.geojson");
                    try {
                        createConnections(corruptedJson, poiMap);
                    } catch (IOException | JSONException e) {
                        throw new RuntimeException(e);
                    }
                    findIsolatedPOI(poiMap);
                }
            }
            // If no .geojson files were found
            if (!geoJsonFound) {
                Log.d(TAG, "No GeoJson files found in folder: " + path);
            }
        } else {
            // Log if the folder is empty or does not exist
            Log.d(TAG, "The folder " + path + " is empty or does not exist.");
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
                String map_chosen = spinnerMap.getSelectedItem().toString();
                Log.i("Element gewählt", "Ein Element wurde ausgewählt " + map_chosen);
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
        String nameWithoutExtension = (lastDotIndex == -1) ? name : name.substring(0, lastDotIndex);

        // Capitalize the first letter and concatenate the rest of the name
        if (!nameWithoutExtension.isEmpty()) {
            nameWithoutExtension = nameWithoutExtension.substring(0, 1).toUpperCase() + nameWithoutExtension.substring(1);
        }

        return nameWithoutExtension;
    }
}