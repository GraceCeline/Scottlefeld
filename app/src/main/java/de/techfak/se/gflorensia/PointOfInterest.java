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
import java.util.HashSet;
import java.util.List;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PointOfInterest {
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
    public String getName(){
        return name;
    }
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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

    GeoPoint createGeoPoint(){
       return new GeoPoint(latitude.doubleValue(), longitude.doubleValue());
    }



    GeoPoint marginPOI(){
        return new GeoPoint(latitude.doubleValue()-0.01, longitude.doubleValue()-0.01);
    }

    List<GeoPoint> boundPOI(){
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



    void displayConnection (MapView mapView) {
        for (Connection connection : this.getConnections()) {
            Polyline line = new Polyline();
            line.setPoints(Arrays.asList(this.createGeoPoint(), connection.getDestination().createGeoPoint()));

            // Color coding by transport mode
            Paint paint = line.getPaint();
            switch (connection.getTransportMode()) {
                case "bus":
                    paint.setColor(Color.BLUE);
                    break;
                case "escooter":
                    paint.setColor(Color.RED);
                    break;
                case "tram":
                    paint.setColor(Color.GREEN);
                    break;
            }

            paint.setStrokeWidth(connection.getTransportMode().length() > 1 ? 10f : 5f);

            mapView.getOverlays().add(line);

        }
    }
}