package de.techfak.se.gflorensia;

import androidx.appcompat.app.AlertDialog;

public class CorruptedMapException extends Exception {
    public CorruptedMapException() {
        super("You picked a map with isolated POIs!");
    }
}
