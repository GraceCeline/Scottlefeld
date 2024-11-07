package de.techfak.se.gflorensia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.TaskStackBuilder;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StartActivity extends BaseActivity {

    String selectedPOI;
    String selectedTransportMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_start);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String mapName = getIntent().getExtras().getString("chosen_map");
        String filename = mapName + ".geojson";
        Collection<PointOfInterest> poiCollection = null;
        Spinner spinnerPOI = findViewById(R.id.spinner2);
        Spinner spinnerTransport = findViewById(R.id.spinner3);
        AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);

        try {
            loadGameMap(mapName); // Attempt to load map data
            poiCollection = loadGameMap(mapName).values();

        } catch (CorruptedMapException | JSONException |
                 IOException e) {  // Catch the exception here
            showCorruptedMapDialog();  // Call dialog to handle the corrupted map
        }
        List<PointOfInterest> poiList = new ArrayList<>(poiCollection);
        PointOfInterest randomPOI = getRandomPOI(poiList); //Pick a random POI

        TextView textView = findViewById(R.id.textView2);
        textView.setText(randomPOI.getName());
        Log.i("POI selected",randomPOI.getName());

        List<String> connectionList;
        try {
            connectionList = getConnectedPOIs(randomPOI);
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                connectionList.toArray()
        );
        spinnerPOI.setAdapter(adapter);



        spinnerPOI.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPOI = spinnerPOI.getSelectedItem().toString();
                Log.i("Element gewählt", "Ein POI wurde ausgewählt " + selectedPOI);

                PointOfInterest destination = getDestinationPOI(selectedPOI, poiList);
                if (destination != null){
                    List<String> availableTransportModes = getTransportModeforPOI(randomPOI, destination);
                    updateDropdown(spinnerTransport, availableTransportModes);
                }

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

        spinnerTransport.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTransportMode = spinnerTransport.getSelectedItem().toString();

                Log.i("Selected Transport Mode", selectedTransportMode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: handle case where nothing is selected in the second dropdown
            }
        });
    }


    public void showCorruptedMapDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Corrupted Map")
                .setMessage("You picked a map with isolated POIs!") // Display the exception message
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    // Navigate back to MainActivity after acknowledging the error
                    Intent intent = new Intent(StartActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Close GameActivity
                })
                .show();
    }

    public Map<String, PointOfInterest> loadGameMap(String mapChosen) throws CorruptedMapException, JSONException, IOException {
        if (Objects.equals(mapChosen, "corrupted")){
            throw new CorruptedMapException();
        }
        String filename = mapChosen + ".geojson";
        String mapJson = getJsonContent("maps/" + filename);
        Map<String, PointOfInterest> poiMap = extractPOI(mapJson);
        createConnections(mapJson, poiMap);
        return poiMap;
    }

    public PointOfInterest getRandomPOI(List<PointOfInterest> poiList) {
        Random random = new Random();
        int randomIndex = random.nextInt(poiList.size());
        return poiList.get(randomIndex);
    }

    public List<String> getConnectedPOIs (PointOfInterest randomPOI) throws JSONException, IOException {
        List<String> destinationListwithDups = new ArrayList<>();

        for (Connection connection : randomPOI.getConnections()){
            destinationListwithDups.add(connection.getDestination().getName());
        }

        Set<String> set = new HashSet<>(destinationListwithDups);
        List<String> destinationList = new ArrayList<>(set);

        return destinationList;
    }

    PointOfInterest getDestinationPOI (String poiName, List<PointOfInterest> poiList){
        for ( PointOfInterest poi : poiList){
            if (poi.getName().equals(poiName)){
                Log.i("Destination", poi.getName());
                return poi;
            }
        }
        return null;
    }

    public List<String> getTransportModeforPOI (PointOfInterest randomPOI, PointOfInterest destinationPOI){
        List<String> transportmodeList = new ArrayList<>();
        List<Connection> poiConnection = randomPOI.getConnections();
        for (Connection connection : poiConnection){
            if (connection.getDestination().equals(destinationPOI)) {
                transportmodeList.add(connection.getTransportMode());
            }
        }
        return transportmodeList;
    }

    private void updateDropdown(Spinner dropdown, List<String> options) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);
    }


}



