package de.techfak.se.gflorensia;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * This is the MainActivity that is executed when the app is started.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
