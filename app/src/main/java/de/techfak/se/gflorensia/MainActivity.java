package de.techfak.se.gflorensia;
import java.util.HashMap;
import java.util.Map;
import androidx.appcompat.app.AppCompatActivity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.Math;
import java.math.BigDecimal;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
/**
 * This is the MainActivity that is executed when the app is started.
 */
public class MainActivity extends AppCompatActivity {
    String TAG = "GeoJson";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager assetManager = getAssets();
        String path = "maps";
        Map<String,PointOfInterest> poiMap = new HashMap<>();
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
    }
    /**
     * Returns a list with all filenames of a folder inside the assets.
     * Pay attention that a empty list is returned if the folder does not exists.
     *
     * @param path The path of the folder. Relative to the assets folder.
     * @return A list of the files inside the folder or null if an error occurred.
     */
    public List<String> getFolder(String path) {
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
    public InputStream getFileInputStream(String path) {
        if (path == null) {
            return null;
        }
        try {
            return getAssets().open(path);
        } catch (IOException e) {
            return null;
        }
    }
    public String getJsonContent(String filePath){
        BufferedReader br = new BufferedReader(
                new InputStreamReader(getFileInputStream(filePath))
        );
        return br.lines().collect(Collectors.joining());
    }
    public Map<String,PointOfInterest> extractPOI(String jsonContent) throws JSONException {
        ObjectMapper om = new ObjectMapper();
        Map<String,PointOfInterest> poiMap = new HashMap<>();
        try {
            JsonNode root = om.readTree(jsonContent);
            for (JsonNode jn : root.get("features")) {
                String featureType = jn.get("geometry").get("type").asText();
                if (featureType.equals("Point")) {
                    String name = jn.get("properties").get("name").asText();
                    JsonNode coordinates = jn.get("geometry").get("coordinates");
                    // Extract longitude and latitude from the coordinates array
                    BigDecimal latitude = jn.get("geometry").get("coordinates").get(0).decimalValue();
                    BigDecimal longitude = jn.get("geometry").get("coordinates").get(1).decimalValue();
                    // Create a new PointOfInterest object
                    PointOfInterest poi = new PointOfInterest(name, latitude, longitude);
                    poiMap.put(name, poi);
                    // Optionally log the POI
                    Log.i("POI",poi.describePOI());
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return poiMap;
    }
    public void createConnections(String jsonContent, Map<String, PointOfInterest> poiMap) throws IOException, JSONException {
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
                        PointOfInterest poi = poiMap.get(destination.get("p1").asText());
                        Connection connection = new Connection(transportMode, poi);
                        assert poi != null;
                        poi.addConnection(connection);
                        Log.i("Connection", connection.describeConnection());
                    }
                    if (poiMap.containsKey(destination.get("p2").asText())) {
                        PointOfInterest poi = poiMap.get(destination.get("p2").asText());
                        Connection connection = new Connection(transportMode, poi);
                        assert poi != null;
                        poi.addConnection(connection);
                        Log.i("Connection", connection.describeConnection());
                    }
                }
            }
        }
    }
    public List<PointOfInterest> findIsolatedPOI(Map<String,PointOfInterest> poiMap){
        List<PointOfInterest> isolatedPOIs = new ArrayList<>();
        for (PointOfInterest poi : poiMap.values()) {
            boolean isIsolated = poi.getConnections().isEmpty(); // No outgoing connections
            if (isIsolated) {
                isolatedPOIs.add(poi);
                Log.i("Isolated",poi.getName() + " is isolated");
            }
        }
        return isolatedPOIs;
    }
}