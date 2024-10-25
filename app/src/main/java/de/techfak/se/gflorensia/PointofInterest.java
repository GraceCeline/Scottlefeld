package de.techfak.se.gflorensia;
import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.math.BigDecimal;
import java.util.Objects;

class PointOfInterest {
    private String name;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<Connection> connections = new ArrayList<>();

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


}
