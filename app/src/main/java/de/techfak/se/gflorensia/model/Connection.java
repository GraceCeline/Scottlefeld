package de.techfak.se.gflorensia.model;

public class Connection {

    String transportMode;
    PointOfInterest destination;
    public Connection(String transportMode, PointOfInterest destination) {
        this.transportMode = transportMode;
        this.destination = destination;
    }

    public String getTransportMode() {
        return this.transportMode;
    }

    public PointOfInterest getDestination() {
        return this.destination;
    }

    String describeConnection() {
        return "Connection with " + this.transportMode + " to " + this.destination.getName();
    }

}
