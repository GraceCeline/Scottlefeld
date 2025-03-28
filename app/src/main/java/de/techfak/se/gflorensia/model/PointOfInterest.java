package de.techfak.se.gflorensia.model;

import org.json.JSONException;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.math.BigDecimal;
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
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        PointOfInterest that = (PointOfInterest) object;
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

    public GeoPoint createGeoPoint() {
       return new GeoPoint(latitude.doubleValue(), longitude.doubleValue());
    }

    public GeoPoint marginPOI() {
        return new GeoPoint(latitude.doubleValue() - MARGIN, longitude.doubleValue() - MARGIN);
    }

    public List<GeoPoint> boundPOI() {
        List<GeoPoint> boundaries = new ArrayList<>();
        boundaries.add(this.createGeoPoint());
        boundaries.add(this.marginPOI());
        return boundaries;
    }

    public List<String> getConnectedPOIs() throws JSONException, IOException {
        List<String> destinationListwithDups = new ArrayList<>();

        for (Connection connection : this.getConnections()) {
            destinationListwithDups.add(connection.getDestination().getName());
        }

        Set<String> set = new HashSet<>(destinationListwithDups);
        return new ArrayList<>(set);
    }
}
