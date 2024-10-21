package de.techfak.se.gflorensia;

import androidx.appcompat.app.AppCompatActivity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

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
}
