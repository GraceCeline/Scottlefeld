package de.techfak.se.gflorensia;
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
        return new GeoPoint(latitude.doubleValue()-0.03, longitude.doubleValue()-0.03);
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

    public List<String> getTransportModeforPOI(PointOfInterest destinationPOI){
        List<String> transportmodeList = new ArrayList<>();
        List<Connection> poiConnection = this.getConnections();
        for (Connection connection : poiConnection){
            if (connection.getDestination().equals(destinationPOI)) {
                transportmodeList.add(connection.getTransportMode());
            }
        }
        return transportmodeList;
    }
}