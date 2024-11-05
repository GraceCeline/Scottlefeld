package de.techfak.se.gflorensia;

public class CorruptedMapException extends Exception {
    public CorruptedMapException() {
        super("You picked a map with isolated POIs!");
    }
}
