package com.fleet.telematics.domain.model;

import com.fleet.telematics.domain.entity.TelematicsEvent;

public class IngestionResult {

    private final IngestStatus status;
    private final String eventId;
    private final String message;
    private final TelematicsEvent savedEvent;

    public IngestionResult(IngestStatus status, String eventId, String message, TelematicsEvent savedEvent) {
        this.status = status;
        this.eventId = eventId;
        this.message = message;
        this.savedEvent = savedEvent;
    }

    public static IngestionResult processed(TelematicsEvent savedEvent) {
        return new IngestionResult(
                IngestStatus.PROCESSED,
                savedEvent.getEventId(),
                "Telemetry event ingested successfully",
                savedEvent
        );
    }

    public static IngestionResult duplicate(String eventId) {
        return new IngestionResult(
                IngestStatus.DUPLICATE_IGNORED,
                eventId,
                "Duplicate event detected and ignored gracefully",
                null
        );
    }

    public static IngestionResult rejected(String eventId, String reason) {
        return new IngestionResult(
                IngestStatus.REJECTED,
                eventId,
                reason,
                null
        );
    }

    public IngestStatus getStatus() {
        return status;
    }

    public String getEventId() {
        return eventId;
    }

    public String getMessage() {
        return message;
    }

    public TelematicsEvent getSavedEvent() {
        return savedEvent;
    }
}
