package de.techfak.se.gflorensia;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
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

import com.google.android.material.snackbar.Snackbar;

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
import java.util.stream.Collectors;

import de.techfak.gse24.botlib.MX;
import de.techfak.gse24.botlib.PlayerFactory;
import de.techfak.gse24.botlib.Turn;
import de.techfak.gse24.botlib.exceptions.JSONParseException;
import de.techfak.gse24.botlib.exceptions.NoFreePositionException;
import de.techfak.gse24.botlib.exceptions.NoTicketAvailableException;

public class StartActivity extends BaseActivity implements PropertyChangeListener{

    String selectedPOI;
    String selectedTransportMode;
    PointOfInterest currentLocation;
    PointOfInterest destination;
    MapView mapView;
    View view;

    TextView center;
    Marker marker;
    Marker mx = null;
    Polyline line;
    Player player;

    PlayerFactory playerFactory;
    GameApplication game;
    MX mxPlayer;
    String mxPosition = "";

    Set<Integer> showMXrounds = new HashSet<>(Arrays.asList(3, 8, 13, 18));

    TextView roundCounter;
    TextView busTicket;
    TextView escooterTicket;
    TextView tramTicket;


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
        roundCounter = findViewById(R.id.textView7);
        line = new Polyline();
        game = (GameApplication) getApplication();
        view = findViewById(R.id.main);
        Context ctx = getApplicationContext();

        IConfigurationProvider provider = Configuration.getInstance();
        provider.setUserAgentValue(ctx.getPackageName());
        provider.load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        player = new Player();
        game.setPlayer(player);
        game.getPlayer().addListener(this);

        // Right after game is started
        try {
            loadGameMap(mapName); // Attempt to load map data
            poiCollection = loadGameMap(mapName).values();
            Log.i("POI Collection", poiCollection.toString());
            game.player.incRound();

            extractTickets(getJsonContent("maps/"+ mapName +".geojson"), game.detectiveTickets, game.mxTickets);
            game.player.setBusTickets(game.detectiveTickets.get("bus"));
            game.player.setTramTickets(game.detectiveTickets.get("tram"));
            game.player.setScooterTickets(game.detectiveTickets.get("escooter"));

            mxPlayer = createMX(mapName, player, game.mxTickets);
            Log.i("MX Start", mxPlayer.getPosition());
        } catch (CorruptedMapException | JSONException | IOException e) {  // Catch the exception here
            showErrorMapDialog("Corrupted Map", "You picked a map with isolated POIs!");  // Call dialog to handle the corrupted map
            return;
        } catch (NullPointerException e) {
            Log.e("Ticket", "Encountered a NullPointerException: " + e.getMessage());
        } catch (JSONParseException e) {
            showErrorMapDialog("JsonParseException", "Cannot parse Json");
            return;
        } catch (NoFreePositionException e) {
            showErrorMapDialog("No free Position", "No free positions available! Please choose another action.");
        }

        // List all POI in the map

        List<PointOfInterest> poiList = new ArrayList<>(poiCollection);
        PointOfInterest randomPOI = getRandomPOI(poiList); //Pick a random POI
        currentLocation = randomPOI; //current Location set as random POI
        Log.i("POI selected", randomPOI.getName());
        postMap(randomPOI, poiList); // Display map
        player.setPosition(currentLocation.getName());

        //////////////////////////////////////////////

        // View management
        center = findViewById(R.id.textView3); // Button to center the map
        TextView textView = findViewById(R.id.textView2); // Show the name of the map
        textView.setText(randomPOI.getName()); //Display selected POI in a text view
        Log.i("POI selected", randomPOI.getName());

        // Show all connections

        List<String> connectionList = new ArrayList<>();
        try {
            connectionList = randomPOI.getConnectedPOIs();
        } catch (JSONException | IOException e) {
            // throw new CannotLoadConnectionException();
        }

        // MX Turn
        try {
            Turn turn = mxTurn(mxPlayer);
            Log.i("Bus Ticket", String.valueOf(mxPlayer.getBusTickets()));
            Log.i("Tram Ticket", String.valueOf(mxPlayer.getTramTickets()));
            Log.i("Scooter Ticket", String.valueOf(mxPlayer.getScooterTickets()));
        } catch (NoTicketAvailableException e) {
            Log.i("MX Exception", "No ticket available!");
            Log.i("MX Position", mxPlayer.getPosition());
            Log.i("MX Transport", "none");
        }

        /* Create dropdown menu for Point of Interests */
        ArrayAdapter<String> adapter = new ArrayAdapter(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                connectionList.toArray()
        );
        spinnerPOI.setAdapter(adapter);

        List<String> availableTransportModes;

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

                Log.d("Selected Transport", selectedTransportMode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        finishTurnButton.setOnClickListener(v -> {

            try {
                Map<String, Integer> tickets = game.detectiveTickets;
                boolean allTicketZero = game.returnAllZero(currentLocation, tickets);

                if (allTicketZero || game.player.round == 22){
                    endGame("MX has won the game", poiList);
                } else if (destination.getName().equals(mxPosition)) {
                    endGame("Detective has won the game! Congratulations", poiList);
                }

                AtomicReference<PointOfInterest> randomPOIAtomic = new AtomicReference<>(randomPOI);
                randomPOIAtomic.set(destination);
                center.setText(describeGeoPoint(destination.createGeoPoint()));

                if (destination != null && selectedTransportMode != null) {
                    // Validate move before the next step
                    validateMove(currentLocation, destination, selectedTransportMode, tickets);

                    Log.i("Detective ", "Transport" + selectedTransportMode);

                    // Update current location to new destination
                    textView.setText(randomPOIAtomic.get().getName()); // Update displayed current location
                    currentLocation = destination; // set currentLocation as destination
                    marker.setPosition(destination.createGeoPoint());

                    game.player.incRound();

                        if (Objects.equals(selectedTransportMode, "bus")) {
                            game.player.decBusTickets();
                            mxPlayer.giveBusTicket();
                            Log.i("Player ticket", String.valueOf(player.getBusTickets()));
                            Log.i("MX gets", String.valueOf(mxPlayer.getBusTickets()));
                        } else if (Objects.equals(selectedTransportMode, "tram")) {
                            game.player.decTramTickets();
                            mxPlayer.giveTramTicket();
                            Log.i("Player ticket", String.valueOf(player.getTramTickets()));
                            Log.i("MX gets", String.valueOf(mxPlayer.getTramTickets()));
                        } else if (Objects.equals(selectedTransportMode, "escooter")) {
                            game.player.decScooterTickets();
                            mxPlayer.giveScooterTicket();
                            Log.i("Player ticket", String.valueOf(player.getScooterTickets()));
                            Log.i("MX gets", String.valueOf(mxPlayer.getScooterTickets()));
                        }

                    // Refresh POIs and transport modes based on the new location
                    List<String> newConnections;
                    try {
                        newConnections = randomPOIAtomic.get().getConnectedPOIs();
                    } catch (JSONException | IOException e) {
                        throw new RuntimeException(e);
                    }
                    updateDropdown(spinnerPOI, newConnections); // Update first dropdown with new connections

                    try {
                        Turn turn = mxTurn(mxPlayer);
                        Log.i("Bus Ticket", String.valueOf(mxPlayer.getBusTickets()));
                        Log.i("Tram Ticket", String.valueOf(mxPlayer.getTramTickets()));
                        Log.i("Scooter Ticket", String.valueOf(mxPlayer.getScooterTickets()));
                    } catch (NoTicketAvailableException e) {
                        Log.i("MX Position", mxPlayer.getPosition());
                        Log.i("MX Transport", "none");
                    }

                    showMXMarker(game.player.round, poiList); // Show MX marker on certain rounds

                    // Clear the second dropdown until a new POI is selected
                    updateDropdown(spinnerTransport, Collections.emptyList()); // Set an empty list

                    mapView.invalidate();
                }
            } catch (InvalidConnectionException | ZeroTicketException e) {
                Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG).show();
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
                game.player.removeListener(StartActivity.this);
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


    MX createMX(String mapChosen, Player player, Map<String,Integer> mxTickets) throws JSONParseException, NoFreePositionException {
        String filename = mapChosen + ".geojson";
        String jsonContent = getJsonContent("maps/" + filename);

        playerFactory = new PlayerFactory(jsonContent,player);

        return playerFactory.createMx(mxTickets.get("bus"),mxTickets.get("tram"),mxTickets.get("escooter"));
    }
    Turn mxTurn(MX mxplayer) throws NoTicketAvailableException {
        Turn turn = mxplayer.getTurn();
        mxPosition = mxplayer.getPosition();
        Log.i("MX", "Transport: " +turn.ticketType().toString());
        Log.i("MX","Position: "+ turn.target());

        return turn;
    }



    private void updateDropdown(Spinner dropdown, List<String> options) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);
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

    void showMXMarker(Integer number, List<PointOfInterest> poiList){

        if (showMXrounds.contains(number)) {
            if(mx == null) {
                mx = new Marker(mapView);
                mx.setTitle("MX here, I'm in " + mxPlayer.getPosition());
                mx.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.mx, null));
                PointOfInterest mxPosition = getDestinationPOI(mxPlayer.getPosition(), poiList);
                mx.setPosition(mxPosition.createGeoPoint());
            }
            Log.i("MX Marker", "Position " + mxPlayer.getPosition());


            mapView.getOverlays().add(mx);
            mx.setVisible(true);
            Log.i("Marker", "Shown");
        } else {
            if (mx != null) {
                // Hide the marker if it's not in a valid round
                mx.setVisible(false);
                mapView.getOverlays().remove(mx);
                Log.i("Marker", "Not Shown");
            }
        }
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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Log.i("PropertyChange", "Property changed: " + evt.getPropertyName() + " to " + evt.getNewValue());
        switch (evt.getPropertyName()) {
            case "round":
                roundCounter = findViewById(R.id.textView7);
                String text = "Round " + game.player.getRound();
                roundCounter.setText(text);
                break;
            case "bus":
                busTicket = findViewById(R.id.textView4);
                busTicket.setText(evt.getNewValue().toString());
                break;
            case "escooter":
                escooterTicket = findViewById(R.id.textView5);
                escooterTicket.setText(evt.getNewValue().toString());
                break;
            case "tram":
                tramTicket = findViewById(R.id.textView6);
                tramTicket.setText(evt.getNewValue().toString());
                break;
        }

    }

    private void endGame(String message, List<PointOfInterest> poiList) {
        // Standort von M. X anzeigen
        Marker mxMarker = new Marker(mapView);
        mxMarker.setPosition(getDestinationPOI(mxPosition, poiList).createGeoPoint());
        mxMarker.setTitle("MX Position");
        mxMarker.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.mx, null));
        mapView.getOverlays().add(mxMarker);
        mapView.invalidate();

        // Gewinner anzeigen
        new AlertDialog.Builder(StartActivity.this)
                .setTitle("End of game")
                .setMessage(message)
                .setNeutralButton("Exit", (dialog, id) -> {
                    StartActivity.super.onBackPressed();
                    finish();
                })
                .setCancelable(false)
                .show();
    }


}
