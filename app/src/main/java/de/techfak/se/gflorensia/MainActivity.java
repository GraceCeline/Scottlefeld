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

                    /* if (file.equals("medium.geojson")){
                        String smallJson = getJsonContent(path + "/" + "medium.geojson");
                        try {
                            extractPOI(smallJson);
                            createConnections(smallJson, poiMap);
                        } catch (IOException | JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }*/
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
        String nameWithoutExtension = (lastDotIndex == -1) ? name : name.substring(0, lastDotIndex);
        return nameWithoutExtension;
    }


    public void onClick(View view) {
        Intent intent = new Intent(this, StartActivity.class);
        intent.putExtra("chosen_map",selectedMap);
        startActivity(intent);
    }

}