package com.fleet.telematics.exception;

public class DuplicateEventException extends RuntimeException {
    private final String eventId;

    public DuplicateEventException(String eventId) {
        super("Duplicate telemetry event detected with eventId: " + eventId);
        this.eventId = eventId;
    }

    public String getEventId() {
        return eventId;
    }
}
