package de.techfak.se.gflorensia;

import java.util.Map;

public class Connection {

    private String transportMode;
    private PointOfInterest destination;
    public Connection(String transportMode, PointOfInterest destination) {
        this.transportMode = transportMode;
        this.destination = destination;
    }

    String describeConnection(){
        return "Connection with " + this.transportMode + " to " + this.destination.getName();
    }
}
