package de.techfak.se.gflorensia;

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
import org.json.JSONObject;
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
                        extractPOI(jsonContent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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

    private void extractPOI(String jsonContent) throws JSONException {

        ObjectMapper om = new ObjectMapper();
        List<PointOfInterest> poiList = new ArrayList<>();

        try {
            JsonNode root = om.readTree(jsonContent);

            //JSONObject geoJson = new JSONObject(geoJsonData);

            //JSONArray features = geoJson.getJSONArray("features");

            // Loop through each feature to extract POIs
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
                    //poiList.add(poi);

                    // Optionally log the POI
                    Log.i("POI",poi.describePOI());
                }

            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        //return poiList;
    }
}