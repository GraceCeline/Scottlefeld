package de.techfak.se.gflorensia;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import de.techfak.gse24.botlib.MX;
import de.techfak.gse24.botlib.PlayerFactory;
import de.techfak.gse24.botlib.Turn;
import de.techfak.gse24.botlib.exceptions.JSONParseException;
import de.techfak.gse24.botlib.exceptions.NoFreePositionException;
import de.techfak.gse24.botlib.exceptions.NoTicketAvailableException;

public class StartActivity extends BaseActivity {

    String selectedPOI;
    String selectedTransportMode;
    PointOfInterest currentLocation;
    PointOfInterest destination;
    private MapView mapView;

    TextView center;
    Marker marker;
    Polyline line;

    PlayerFactory playerFactory;


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
// Datas needed
        String mapName = getIntent().getExtras().getString("chosen_map");
        Collection<PointOfInterest> poiCollection = null;
        Spinner spinnerPOI = findViewById(R.id.spinner2);
        Button finishTurnButton = findViewById(R.id.button3);
        Spinner spinnerTransport = findViewById(R.id.spinner3);
        AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
        mapView = findViewById(R.id.map1);
        line = new Polyline();

        Context ctx = getApplicationContext();

        IConfigurationProvider provider = Configuration.getInstance();
        provider.setUserAgentValue(ctx.getPackageName());
        provider.load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        Player player = new Player();

        // Load game map and create POI Connections
        try {
            loadGameMap(mapName); // Attempt to load map data
            poiCollection = loadGameMap(mapName).values();

            // Create MX
            MX mxPlayer = createMX(mapName, player);
            Log.i("MX Start", mxPlayer.getPosition());
        } catch (CorruptedMapException | JSONException |
                 IOException e) {  // Catch the exception here
            showErrorMapDialog("Corrupted Map", "You picked a map with isolated POIs!");  // Call dialog to handle the corrupted map
            return;
        } catch (JSONParseException e) {
            showErrorMapDialog("JsonParseException", "Cannot parse Json");
            return;
        } catch (NoFreePositionException e) {
            showErrorMapDialog("No free Position", "No free positions available! Please choose another action.");
        }
        List<PointOfInterest> poiList = new ArrayList<>(poiCollection);
        PointOfInterest randomPOI = getRandomPOI(poiList); //Pick a random POI
        currentLocation = randomPOI; //current Location set as random POI
        center = findViewById(R.id.textView3);

        TextView textView = findViewById(R.id.textView2);
        textView.setText(randomPOI.getName()); //Display selected POI in a text view
        Log.i("POI selected", randomPOI.getName());

        postMap(randomPOI, poiList); // Display map

        List<String> connectionList = new ArrayList<>();
        try {
            connectionList = randomPOI.getConnectedPOIs();
        } catch (JSONException | IOException e) {
            // throw new CannotLoadConnectionException();
        }
        /* Create dropdown menu for Point of Interests */
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

                destination = getDestinationPOI(selectedPOI, poiList);
                if (destination != null) {
                    List<String> availableTransportModes = getTransportModeforPOI(currentLocation, destination);
                    updateDropdown(spinnerTransport, availableTransportModes);
                    Log.i("Transport Modes", availableTransportModes.toString());

                    updatePolyline(currentLocation.createGeoPoint(), destination.createGeoPoint());
                }

            }
            /* Dropdown Menu POI done */

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
            }
        });

        finishTurnButton.setOnClickListener(v -> {
            AtomicReference<PointOfInterest> randomPOIAtomic = new AtomicReference<>(randomPOI);
            randomPOIAtomic.set(destination);
            center.setText(describeGeoPoint(destination.createGeoPoint()));

            if (destination != null && selectedTransportMode != null) {
                // Update current location to new destination
                textView.setText(randomPOIAtomic.get().getName()); // Update displayed current location
                currentLocation = destination; // set currentLocation as destination
                marker.setPosition(destination.createGeoPoint());
                // postMap(destination);

                // Refresh POIs and transport modes based on the new location
                List<String> newConnections;
                try {
                    newConnections = randomPOIAtomic.get().getConnectedPOIs();
                } catch (JSONException | IOException e) {
                    throw new RuntimeException(e);
                }
                updateDropdown(spinnerPOI, newConnections); // Update first dropdown with new connections

                // Clear the second dropdown until a new POI is selected
                spinnerTransport.setAdapter(null);
            }
        });

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(StartActivity.this)
                        .setMessage("Are you sure you want to exit?")
                        .setPositiveButton("Yes", (dialog, id) -> {
                            StartActivity.super.onBackPressed();
                            finish();
                        })
                        .setNegativeButton("No", (dialog, id) -> {
                            dialog.dismiss();
                        })
                        .show();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        Button button_center = findViewById(R.id.button2);
        button_center.setOnClickListener(v -> {
            if (currentLocation != null) {
                mapView.getController().setCenter(currentLocation.createGeoPoint());
                center.setText(describeGeoPoint(currentLocation.createGeoPoint()));
                Log.i("Center", describeGeoPoint(currentLocation.createGeoPoint()));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    public void showErrorMapDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    Intent intent = new Intent(StartActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Close GameActivity
                })
                .show();
    }

    public Map<String, PointOfInterest> loadGameMap(String mapChosen) throws CorruptedMapException, JSONException, IOException {
        String filename = mapChosen + ".geojson";
        String mapJson = getJsonContent("maps/" + filename);
        Map<String, PointOfInterest> poiMap = extractPOI(mapJson);
        createConnections(mapJson, poiMap);

        for (PointOfInterest poi : poiMap.values()) {
            if (poi.getConnections().isEmpty()) {
                throw new CorruptedMapException();
            }
        }

        return poiMap;
    }


    MX createMX(String mapChosen, Player player) throws JSONParseException, NoFreePositionException {
        String filename = mapChosen + ".geojson";
        String jsonContent = getJsonContent("maps/" + filename);

        playerFactory = new PlayerFactory(jsonContent,player);
        Log.i("Player Bus ticket", String.valueOf(player.getBusTickets()));
        MX mxPlayer = playerFactory.createMx(1,1,1) ; // Tickets will be extracted in US 11

        return mxPlayer;
    }
    public PointOfInterest getRandomPOI(List<PointOfInterest> poiList) {
        Random random = new Random();
        int randomIndex = random.nextInt(poiList.size());
        return poiList.get(randomIndex);
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

    String describeGeoPoint(GeoPoint geo){
        return "Latitude " + geo.getLatitude()+ " Longitude " + geo.getLongitude();
    }

    void postMap(PointOfInterest poi_chosen, List<PointOfInterest> poiList){

        mapView.post(() -> {
            mapView.zoomToBoundingBox(BoundingBox.fromGeoPointsSafe(poi_chosen.boundPOI()), false);

            // mapView.getController().setCenter(poi.createGeoPoint());
            center.setText(describeGeoPoint(poi_chosen.createGeoPoint()));

            marker = new Marker(mapView);

            marker.setPosition(poi_chosen.createGeoPoint());

            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(currentLocation.getName());
            marker.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.person, null));

            mapView.getOverlays().add(marker);

            for (PointOfInterest poi: poiList) {
                if (poi == currentLocation)
                    continue;


                Marker marker = new Marker(mapView);
                marker.setPosition(poi.createGeoPoint());
                marker.setIcon(ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_menu_mylocation, null));
                marker.setOnMarkerClickListener((m, map) -> {
                    Toast.makeText(this, "Point of Interest: " + poi.getName(), Toast.LENGTH_SHORT).show();
                    return true;

                });
                poi.displayConnection(mapView);
                mapView.getOverlays().add(marker);
            }

        });
    }

    void updatePolyline(GeoPoint startPoint, GeoPoint endPoint) {
        // Remove the existing polyline
        if (line != null) {
            mapView.getOverlays().remove(line);
        }

        // Create a new polyline
        line = new Polyline();
        line.setPoints(Arrays.asList(startPoint, endPoint));

        // Add the polyline to the map
        mapView.getOverlays().add(line);
        mapView.invalidate(); // Refresh the map
    }



}



