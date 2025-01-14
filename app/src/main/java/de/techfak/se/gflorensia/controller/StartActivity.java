package de.techfak.se.gflorensia.controller;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import de.techfak.gse24.botlib.MX;
import de.techfak.gse24.botlib.PlayerFactory;
import de.techfak.gse24.botlib.Turn;
import de.techfak.gse24.botlib.exceptions.JSONParseException;
import de.techfak.gse24.botlib.exceptions.NoFreePositionException;
import de.techfak.gse24.botlib.exceptions.NoTicketAvailableException;
import de.techfak.se.gflorensia.CorruptedMapException;
import de.techfak.se.gflorensia.GameApplication;
import de.techfak.se.gflorensia.InvalidConnectionException;
import de.techfak.se.gflorensia.NoMapSelectedException;
import de.techfak.se.gflorensia.R;
import de.techfak.se.gflorensia.ZeroTicketException;
import de.techfak.se.gflorensia.model.Connection;
import de.techfak.se.gflorensia.model.GameModel;
import de.techfak.se.gflorensia.model.Player;
import de.techfak.se.gflorensia.model.PointOfInterest;

public class StartActivity extends BaseActivity implements PropertyChangeListener {

    public static final String BUS = "bus";
    public static final String TRAM = "tram";
    public static final String SCOOTER = "escooter";
    public static final String MX_WON = "MX has won the game";
    public static final String DETECTIVE_WON = "Detective has won the game! Congratulations";
    public static final int ENDGAME = 22;
    public static final int ROUND_THREE = 3;
    public static final int ROUND_EIGHT = 8;
    public static final int ROUND_THIRTEEN = 13;
    public static final int ROUND_EIGHTEEN = 18;

    static final String MAPS = "maps/";
    static final String GEO = ".geojson";
    static final String ARROW = "->";

    private static final float THICKNESS = 15.0f;
    private static final float DIF = 7;

    Set<Integer> showMXrounds = new HashSet<>(Arrays.asList(ROUND_THREE, ROUND_EIGHT, ROUND_THIRTEEN, ROUND_EIGHTEEN));
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
    GameApplication game;

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
        game.setGameModel(new GameModel());
        GameModel gameModel = game.getGameModel();
        gameModel.setPlayer(player);
        gameModel.getPlayer().addListener(this);
        gameModel.addListener(this);
        Map<String, Integer> detectiveTicketsMap = new HashMap<>();
        Map<String, Integer> mxTicketsMap = new HashMap<>();
        // Right after game is started
        try {
            // loadGameMap(mapName);
            poiCollection = loadGameMap(mapName).values();
            gameModel.incRound();
        } catch (CorruptedMapException | JSONException | IOException e) {
            showErrorMapDialog("Corrupted Map", "You picked a map with isolated POIs!");
            return;
        } catch (NullPointerException e) {
            Log.i("Debug", e.getMessage());
        }

        String jsonContent = getJsonContent(MAPS + mapName + GEO);
        try {
            extractTickets(jsonContent, detectiveTicketsMap, mxTicketsMap);
            gameModel.setDetectiveTickets(detectiveTicketsMap);
            gameModel.setMXTickets(mxTicketsMap);
            gameModel.getPlayer().setBusTickets(gameModel.getDetectiveTickets().get(BUS));
            gameModel.getPlayer().setTramTickets(gameModel.getDetectiveTickets().get(TRAM));
            gameModel.getPlayer().setScooterTickets(gameModel.getDetectiveTickets().get(SCOOTER));
            MX mxPlayer = createMX(jsonContent, gameModel.getPlayer(), gameModel.getMXTickets());
            gameModel.setMX(mxPlayer);
            Log.i("MX Start", gameModel.getMX().getPosition());
        } catch (JsonProcessingException e) {
            showErrorMapDialog("JsonProcessingException", e.getMessage());
        } catch (JSONParseException e) {
            showErrorMapDialog("JsonParseException", "Cannot parse Json");
            return;
        } catch (NoFreePositionException e) {
            showErrorMapDialog(
                    "No free Position",
                    "No free positions available! Please choose another action."
            );
        } catch (NullPointerException e) {
            showErrorMapDialog("Null Pointer Exception", e.getMessage());
        }

        List<PointOfInterest> poiList = new ArrayList<>(poiCollection);
        gameModel.setPOIList(poiList);
        PointOfInterest randomPOI = getRandomPOI(poiList);
        gameModel.setCurrentLocation(randomPOI);

        // Show the map
        postMap(randomPOI, poiList);
        // Display all connections with lines
        displayConnection(mapView, poiList);
        player.setPosition(gameModel.getCurrentLocation().getName());

        //////////////////////////////////////////////

        // View management
        center = findViewById(R.id.textView3);
        TextView textView = findViewById(R.id.textView2);
        textView.setText(randomPOI.getName());

        // Show all connections

        List<String> connectionList = new ArrayList<>();
        try {
            connectionList = randomPOI.getConnectedPOIs();
        } catch (JSONException | IOException e) {
            showErrorMapDialog("Exception", e.getMessage());
        }

        // MX Turn
        try {
            Turn turn = gameModel.getMX().getTurn();
        } catch (NoTicketAvailableException e) {
            showErrorMapDialog("MX Exception", "No ticket available!");
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
                    List<String> availableTransportModes = getTransportModeforPOI(
                            gameModel.getCurrentLocation(), destination);
                    updateDropdown(spinnerTransport, availableTransportModes);
                    Log.i("Transport Modes", availableTransportModes.toString());
                    updatePolyline(gameModel.getCurrentLocation().createGeoPoint(),
                            destination.createGeoPoint());
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
                    showErrorMapDialog("No Map Selected Exception", e.getMessage());
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
                if (gameModel.getPlayer().returnAllZero(gameModel.getCurrentLocation())) {
                    Log.i("ALl ticket", "zero");
                    endGame(MX_WON, poiList);
                } else if (gameModel.getRound() == ENDGAME) {
                    Log.i("Game", String.valueOf(gameModel.getRound()));
                    endGame(MX_WON, poiList);
                } else if (destination.getName().equals(gameModel.getMX().getPosition())) {
                    endGame("Detective has won the game! Congratulations", poiList);
                }

                AtomicReference<PointOfInterest> randomPOIAtomic = new AtomicReference<>(randomPOI);
                randomPOIAtomic.set(destination);
                center.setText(describeGeoPoint(destination.createGeoPoint()));

                if (destination != null && selectedTransportMode != null) {
                    // Validate move before the next step
                    validateMove(gameModel.getCurrentLocation(), destination, selectedTransportMode, gameModel.getPlayer());

                    Log.i("Detective ", "Transport" + selectedTransportMode);

                    // Update current location to new destination
                    textView.setText(randomPOIAtomic.get().getName());
                    gameModel.setCurrentLocation(destination);
                    marker.setPosition(destination.createGeoPoint());

                    gameModel.incRound();
                    gameModel.manageTickets(selectedTransportMode);

                    // Refresh POIs and transport modes based on the new location
                    List<String> newConnections;
                    try {
                        newConnections = randomPOIAtomic.get().getConnectedPOIs();
                        updateDropdown(spinnerPOI, newConnections);
                    } catch (JSONException | IOException e) {
                        Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }

                    try {
                        Turn turn = gameModel.getMX().getTurn();
                    } catch (NoTicketAvailableException e) {
                        Snackbar.make(view, e.getMessage(),Snackbar.LENGTH_LONG).show();
                        Log.i("MX Position", gameModel.getMX().getPosition());
                    }


                    // Clear the second dropdown until a new POI is selected
                    updateDropdown(spinnerTransport, Collections.emptyList());

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
                gameModel.getPlayer().removeListener(StartActivity.this);
                gameModel.removeListener(StartActivity.this);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        Button buttonCenter = findViewById(R.id.button2);
        buttonCenter.setOnClickListener(v -> {
            if (gameModel.getCurrentLocation() != null) {
                mapView.getController().setCenter(gameModel.getCurrentLocation().createGeoPoint());
                center.setText(describeGeoPoint(gameModel.getCurrentLocation().createGeoPoint()));
                Log.i("Center", describeGeoPoint(gameModel.getCurrentLocation().createGeoPoint()));
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
                    finish();
                })
                .show();
    }

    public Map<String, PointOfInterest> loadGameMap(String mapChosen)
            throws CorruptedMapException, JSONException, IOException {
        String filename = mapChosen + GEO;
        String mapJson = getJsonContent(MAPS + filename);
        Map<String, PointOfInterest> poiMap = extractPOI(mapJson);
        createConnections(mapJson, poiMap);
        for (PointOfInterest poi : poiMap.values()) {
            if (poi.getConnections().isEmpty()) {
                throw new CorruptedMapException();
            }
        }
        return poiMap;
    }

    private void updateDropdown(Spinner dropdown, List<String> options) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);
    }

    void postMap(PointOfInterest poiChosen, List<PointOfInterest> poiList) {
        mapView.post(() -> {
            mapView.zoomToBoundingBox(BoundingBox.fromGeoPointsSafe(poiChosen.boundPOI()), false);

            center.setText(describeGeoPoint(poiChosen.createGeoPoint()));

            marker = new Marker(mapView);

            marker.setPosition(poiChosen.createGeoPoint());

            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(game.getGameModel().getCurrentLocation().getName());
            marker.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.person, null));

            mapView.getOverlays().add(marker);

            for (PointOfInterest poi: poiList) {
                if (poi == currentLocation) {
                    continue;
                }


                Marker marker = new Marker(mapView);
                marker.setPosition(poi.createGeoPoint());
                marker.setIcon(
                        ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_menu_mylocation, null)
                );
                marker.setOnMarkerClickListener((m, map) -> {
                    Toast.makeText(this, "Point of Interest: " + poi.getName(), Toast.LENGTH_SHORT).show();
                    return true;
                });

                mapView.getOverlays().add(marker);
            }
        });

    }
    MX createMX(String jsonContent, Player player, Map<String, Integer> mxTickets)
            throws JSONParseException, NoFreePositionException {
        Log.i("Player", player.toString());
        PlayerFactory playerFactory = new PlayerFactory(jsonContent, player);
        return playerFactory.createMx(mxTickets.get(BUS), mxTickets.get(TRAM), mxTickets.get(SCOOTER));
    }

    void showMXMarker(Integer number, List<PointOfInterest> poiList) {
        String position = game.getGameModel().getMX().getPosition();
        if (showMXrounds.contains(number)) {
            if (mx == null) {
                mx = new Marker(mapView);
                mx.setTitle("MX here, I'm in " + position);
                mx.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.mx, null));
                PointOfInterest mxPosition = getDestinationPOI(position, poiList);
                mx.setPosition(mxPosition.createGeoPoint());
            }
            Log.i("MX Marker", "Position " + position);

            mapView.getOverlays().add(mx);
            mx.setVisible(true);
        } else {
            if (mx != null) {
                // Hide the marker if it's not in a valid round
                mx.setVisible(false);
                mapView.getOverlays().remove(mx);
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
        mapView.invalidate();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Log.i("PropertyChange", "Property changed: " + evt.getPropertyName() + " to " + evt.getNewValue());
        switch (evt.getPropertyName()) {
            case "round":
                roundCounter = findViewById(R.id.textView7);
                String text = "Round " + game.getGameModel().getRound();
                roundCounter.setText(text);
                showMXMarker(game.getGameModel().getRound(), game.getGameModel().getPoiList());
                break;
            case BUS:
                busTicket = findViewById(R.id.textView4);
                busTicket.setText(evt.getNewValue().toString());
                break;
            case SCOOTER:
                escooterTicket = findViewById(R.id.textView5);
                escooterTicket.setText(evt.getNewValue().toString());
                break;
            case TRAM:
                tramTicket = findViewById(R.id.textView6);
                tramTicket.setText(evt.getNewValue().toString());
                break;
            default:
                break;
        }

    }

    private void endGame(String message, List<PointOfInterest> poiList) {
        // Standort von M. X anzeigen
        Marker mxMarker = new Marker(mapView);
        mxMarker.setPosition(getDestinationPOI(game.getGameModel().getMX().getPosition(), poiList).createGeoPoint());
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

    public static void validateMove(PointOfInterest poiStart,
                                    PointOfInterest destinationPOI,
                                    String transportMode,
                                    Player player)
            throws InvalidConnectionException, ZeroTicketException {

        boolean connectionFound = false;
        for (Connection connection : poiStart.getConnections()) {
            if (connection.getDestination().equals(destinationPOI)) {
                if (connection.getTransportMode().equals(transportMode)) {
                    connectionFound = true;
                    break;
                }
            }
        }

        if (!connectionFound) {
            throw new InvalidConnectionException("Invalid connection");
        }

        switch (transportMode) {
            case BUS:
                if (player.getBusTickets() == 0) {
                    throw new ZeroTicketException("No ticket available for bus");
                }
                break;
            case SCOOTER:
                if (player.getScooterTickets() == 0) {
                    throw new ZeroTicketException("No ticket available for scooter");
                }
                break;
            case TRAM:
                if (player.getTramTickets() == 0) {
                    throw new ZeroTicketException("No ticket available for tram");
                }
                break;
            default:
                break;
        }

        Log.i("Validation", "Move is valid");
    }

    void displayConnection(MapView mapView, List<PointOfInterest> poiList) {
        // Map to group connections by unique pair and their transport modes
        Map<String, List<String>> groupedConnections = new HashMap<>();

// Iterate over all POIs and their connections
        for (PointOfInterest poi : poiList) {
            for (Connection connection : poi.getConnections()) {
                PointOfInterest destinationPOI = connection.getDestination();
                String transportMode = connection.getTransportMode();

                // Create a unique key for the connection (order matters to avoid duplicates)
                String connectionKey = createConnectionKey(poi, destinationPOI);

                // Add transport mode to the list for this connection key
                groupedConnections.computeIfAbsent(connectionKey, k -> new ArrayList<>()).add(transportMode);
            }
        }

// Draw lines for each unique connection
        for (Map.Entry<String, List<String>> entry : groupedConnections.entrySet()) {
            String connectionKey = entry.getKey();
            List<String> transportModes = new ArrayList<>(new HashSet<>(entry.getValue()));
            Log.i("Key", connectionKey);
            Log.i("Value", transportModes.toString());

            // Parse the key to get the start and destination POIs
            String[] keyParts = connectionKey.split(ARROW);
            PointOfInterest startPOI = getPOIByName(keyParts[0], poiList);
            PointOfInterest destinationPOI = getPOIByName(keyParts[1], poiList);
 // Base thickness for the lines

            // Iterate over transport modes and draw lines
            for (int i = 0; i < transportModes.size(); i++) {
                String transportMode = transportModes.get(i);

                // Adjust thickness based on transport mode index
                float thickness = THICKNESS - (i * DIF);

                // Create a polyline for this connection
                Polyline line = new Polyline();
                line.setPoints(Arrays.asList(
                        startPOI.createGeoPoint(),
                        destinationPOI.createGeoPoint()
                ));

                // Set line color based on the transport mode
                Paint paint = line.getOutlinePaint();
                switch (transportMode) {
                    case BUS:
                        paint.setColor(Color.BLUE);
                        break;
                    case SCOOTER:
                        paint.setColor(Color.RED);
                        break;
                    case TRAM:
                        paint.setColor(Color.GREEN);
                        break;
                    default:
                        break;
                }
                line.getOutlinePaint().setStrokeWidth(thickness);

                // Add the polyline to the map
                mapView.getOverlayManager().add(line);
            }
        }
        mapView.invalidate();

    }

    String createConnectionKey(PointOfInterest startPoi, PointOfInterest endPoi) {
        String poiStart = startPoi.getName();
        String poiEnd = endPoi.getName();
        if (poiStart.compareTo(poiEnd) < 0) {
            return poiStart + ARROW + poiEnd;
        } else {
            return poiEnd + ARROW + poiStart;
        }
    }

    PointOfInterest getPOIByName(String name, List<PointOfInterest> poiList) {
        for (PointOfInterest poi : poiList) {
            if (poi.getName().equals(name)) {
                return poi;
            }
        }
        return null;
    }

}
