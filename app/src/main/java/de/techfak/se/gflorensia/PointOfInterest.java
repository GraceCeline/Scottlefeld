package de.techfak.se.gflorensia;
import android.graphics.Color;
import android.graphics.Paint;

import org.json.JSONException;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class PointOfInterest {
    private static final double MARGIN = 0.01;
    private static final float THICKNESS = 5.0f;
    String name;
    BigDecimal latitude;
    BigDecimal longitude;
    List<Connection> connections = new ArrayList<>();
    public PointOfInterest(String name, BigDecimal latitude, BigDecimal longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public String describePOI() {
        return "Name: " + name + ", Lat: " + latitude + ", Long: " + longitude;
    }

    public String getName() {
        return name;
    }
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PointOfInterest that = (PointOfInterest) o;
        return name.equals(that.name) && longitude.equals(that.longitude) && latitude.equals(that.latitude);
    }
    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }
    public void addConnection(Connection connection) {
        connections.add(connection);
    }
    public List<Connection> getConnections() {

        return connections;
    }

    GeoPoint createGeoPoint() {
       return new GeoPoint(latitude.doubleValue(), longitude.doubleValue());
    }

    GeoPoint marginPOI() {
        return new GeoPoint(latitude.doubleValue() - MARGIN, longitude.doubleValue() - MARGIN);
    }

    List<GeoPoint> boundPOI() {
        List<GeoPoint> boundaries = new ArrayList<>();
        boundaries.add(this.createGeoPoint());
        boundaries.add(this.marginPOI());
        return boundaries;
    }

    public List<String> getConnectedPOIs() throws JSONException, IOException {
        List<String> destinationListwithDups = new ArrayList<>();

        for (Connection connection : this.getConnections()){
            destinationListwithDups.add(connection.getDestination().getName());
        }

        Set<String> set = new HashSet<>(destinationListwithDups);
        return new ArrayList<>(set);
    }


    void displayConnection(MapView mapView) {
        Map<PointOfInterest, List<String>> connectionMap = new HashMap<>();
        for (Connection connection : this.getConnections()) {

            PointOfInterest connectedPOI = connection.getDestination();
            String transportMode = connection.getTransportMode();

            connectionMap.computeIfAbsent(connectedPOI, k -> new ArrayList<>()).add(transportMode);

        }

        for (Map.Entry<PointOfInterest, List<String>> entry : connectionMap.entrySet()) {
            PointOfInterest connectedPOI = entry.getKey();
            List<String> transportModes = entry.getValue();

            for (int i = 0; i < transportModes.size(); i++) {
                String transportMode = transportModes.get(i);

                // Adjust thickness for each transport mode
                float thickness = THICKNESS + (i * 2);
                Polyline line = new Polyline();
                line.setPoints(Arrays.asList(this.createGeoPoint(), connectedPOI.createGeoPoint()));

                Paint paint = line.getPaint();
                switch (transportMode) {
                    case "bus":
                        paint.setColor(Color.BLUE);
                        break;
                    case "escooter":
                        paint.setColor(Color.RED);
                        break;
                    case "tram":
                        paint.setColor(Color.GREEN);
                        break;
                    default:
                        break;
                }
                line.getOutlinePaint().setStrokeWidth(thickness);
                mapView.getOverlays().add(line);
            }
        }
    }

    private String createConnectionKey(PointOfInterest poi1, PointOfInterest poi2) {
        String name1 = poi1.getName();
        String name2 = poi2.getName();
        return (name1.compareTo(name2) < 0) ? name1 + "->" + name2 : name2 + "->" + name1;
    }

    private PointOfInterest getPOIByName(String name, List<PointOfInterest> poiList) {
        for (PointOfInterest poi : poiList) {
            if (poi.getName().equals(name)) {
                return poi;
            }
        }
        return null; // Handle appropriately if POI is not found
    }
}
