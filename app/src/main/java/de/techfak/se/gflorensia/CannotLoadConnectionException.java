package de.techfak.se.gflorensia;

public class CannotLoadConnectionException extends Throwable {
    public CannotLoadConnectionException() {
        super("Error loading connections");
    }
}
