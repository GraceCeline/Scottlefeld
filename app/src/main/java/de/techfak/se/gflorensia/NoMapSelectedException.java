package de.techfak.se.gflorensia;

public class NoMapSelectedException extends Throwable {
    public NoMapSelectedException() {
        super("You haven't chosen a map!");
    }
}

