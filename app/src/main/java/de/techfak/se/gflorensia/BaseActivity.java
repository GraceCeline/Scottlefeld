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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.snackbar.Snackbar;

import java.math.BigDecimal;

import org.json.JSONException;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class BaseActivity extends AppCompatActivity {

        public List<String> getFolder (String path){
            if (path == null) {
                return null;
            }
            try {
                String[] files = getAssets().list(path);
                if (files == null) {
                    return null;
                }
                return Arrays.asList(files);
            } catch (IOException e) {
                return null;
            }
        }
        /**
         * Return the InputStream of a file inside the assets.
         * Pay attention that the InputStream must be closed after use.
         *
         * @param path The path of the file. Relative to the assets folder.
         * @return The file InputStream or null if an error occurred.
         */
        public InputStream getFileInputStream (String path){
            if (path == null) {
                return null;
            }
            try {
                return getAssets().open(path);
            } catch (IOException e) {
                return null;
            }
        }
        public String getJsonContent (String filePath){

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(getFileInputStream(filePath))
            );
            return br.lines().collect(Collectors.joining());
        }
        public Map<String, PointOfInterest> extractPOI (String jsonContent) throws JSONException {
            Map<String, PointOfInterest> poiMap;
            {
                ObjectMapper om = new ObjectMapper();
                poiMap = new HashMap<>();
                try {
                    JsonNode root = om.readTree(jsonContent);
                    for (JsonNode jn : root.get("features")) {
                        String featureType = jn.get("geometry").get("type").asText();
                        if (featureType.equals("Point")) {
                            String name = jn.get("properties").get("name").asText();
                            JsonNode coordinates = jn.get("geometry").get("coordinates");
                            // Extract longitude and latitude from the coordinates array
                            BigDecimal latitude = jn.get("geometry").get("coordinates").get(1).decimalValue();
                            BigDecimal longitude = jn.get("geometry").get("coordinates").get(0).decimalValue();
                            // Create a new PointOfInterest object
                            PointOfInterest poi = new PointOfInterest(name, latitude, longitude);
                            poiMap.put(name, poi);
                            // Optionally log the POI
                            Log.i("POI", poi.describePOI());
                        }
                    }
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return poiMap;
            }
        }

        public void extractTickets(String jsonContent, Map<String, Integer> detectivesTicketsMap, Map<String, Integer> mxTicketsMap) throws JsonProcessingException {
            ObjectMapper om = new ObjectMapper();
            JsonNode root = om.readTree(jsonContent);

                // Navigate to the "types" object within the "metadata"
                JsonNode types = root.get("metadata").get("types");

                // Iterate through each type (tram, bus, escooter, etc.)
                for (JsonNode jn : types) {
                    // Get the transport type (e.g., "tram", "bus", etc.)
                    String transportType = jn.get("name").asText();

                    // Extract ticket amounts for Detectives and M. X
                    int detectivesTickets = jn.has("Ticketanzahl Detectives")
                            ? jn.get("Ticketanzahl Detectives").asInt() : 0; // Default if not present

                    int mxTickets = jn.has("Ticketanzahl M. X")
                            ? jn.get("Ticketanzahl M. X").asInt()
                            : 0; // Default if not present

                    // Add the extracted information to the maps
                    detectivesTicketsMap.put(transportType.replace("-connection", "").toLowerCase(), detectivesTickets);
                    mxTicketsMap.put(transportType.replace("-connection", "").toLowerCase(), mxTickets);
                }
                Log.i("Detectives Tickets", detectivesTicketsMap.toString());
                Log.i("M.X Tickets", mxTicketsMap.toString());
        }
        public void createConnections (String jsonContent, Map < String, PointOfInterest > poiMap) throws
        IOException, JSONException {
            ObjectMapper om = new ObjectMapper();
            // Handle connections (if any)
            JsonNode root = om.readTree(jsonContent);
            for (JsonNode jn : root.get("features")) {
                String featureType = jn.get("geometry").get("type").asText();
                if (featureType.equals("LineString")) {
                    JsonNode destination = jn.get("properties").get("routePoints");
                    JsonNode transport = jn.get("properties").get("typeId");
                    List<String> typeId = new ArrayList<>();
                    for (JsonNode idNode : transport) {
                        typeId.add(idNode.asText());
                    }
                    for (String transportMode : typeId) {
                        if (poiMap.containsKey(destination.get("p1").asText())) {
                            PointOfInterest poiStart = poiMap.get(destination.get("p1").asText());
                            PointOfInterest poi = poiMap.get(destination.get("p2").asText());
                            Connection connection = new Connection(transportMode, poi);
                            assert poi != null;
                            poiStart.addConnection(connection);
                            Log.i("From POI",poiStart.getName());
                            Log.i("Connection", connection.describeConnection());
                        }
                        if (poiMap.containsKey(destination.get("p2").asText())) {
                            PointOfInterest poiStart = poiMap.get(destination.get("p2").asText());
                            PointOfInterest poi = poiMap.get(destination.get("p1").asText());
                            Connection connection = new Connection(transportMode, poi);
                            assert poi != null;
                            poiStart.addConnection(connection);
                            Log.i("From POI",poiStart.getName());
                            Log.i("Connection", connection.describeConnection());
                        }
                    }
                }
            }
        }
        public List<PointOfInterest> findIsolatedPOI (Map < String, PointOfInterest > poiMap){
            List<PointOfInterest> isolatedPOIs = new ArrayList<>();
            for (PointOfInterest poi : poiMap.values()) {
                boolean isIsolated = poi.getConnections().isEmpty(); // No outgoing connections
                if (isIsolated) {
                    isolatedPOIs.add(poi);
                    Log.i("Isolated", poi.getName() + " is isolated");
                }
            }
            return isolatedPOIs;
        }

    String describeGeoPoint(GeoPoint geo){
        return "Latitude " + geo.getLatitude()+ " Longitude " + geo.getLongitude();
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
}