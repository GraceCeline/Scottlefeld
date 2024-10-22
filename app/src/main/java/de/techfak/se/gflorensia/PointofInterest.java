package de.techfak.se.gflorensia;
import java.lang.Math;
import java.math.BigDecimal;
import java.util.Objects;

class PointOfInterest {
    private String name;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public PointOfInterest(String name, BigDecimal latitude, BigDecimal longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public String describePOI() {
        return "Name: " + name + ", Lat: " + latitude + ", Long: " + longitude;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointOfInterest that = (PointOfInterest) o;
        return longitude.equals(that.longitude) && latitude.equals(that.latitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }
}
