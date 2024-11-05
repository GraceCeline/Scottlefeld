package de.techfak.se.gflorensia;

import android.content.Intent;
import android.os.Bundle;
import java.util.Random;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StartActivity extends BaseActivity {

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
        try {
            loadGameMap(mapName); // Attempt to load map data
        } catch (CorruptedMapException | JSONException | IOException e) {  // Catch the exception here
            showCorruptedMapDialog();  // Call dialog to handle the corrupted map
        }
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

    public void loadGameMap(String mapChosen) throws CorruptedMapException, JSONException, IOException {
        if (Objects.equals(mapChosen, "corrupted")){
            throw new CorruptedMapException();
        }
        String filename = mapChosen + ".geojson";
        String mapJson = getJsonContent("maps/" + filename);
        Map<String, PointOfInterest> poiMap = extractPOI(mapJson);
        createConnections(mapJson, poiMap);
    }
}



