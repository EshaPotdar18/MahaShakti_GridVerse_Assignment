package com.fleet.telematics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response returned for batch telemetry stream ingestion")
public class BatchIngestResponse {

    @Schema(description = "Total number of events submitted in batch")
    private int totalReceived;

    @Schema(description = "Number of events successfully ingested and saved")
    private int totalProcessed;

    @Schema(description = "Number of duplicate events safely skipped")
    private int totalDuplicatesSkipped;

    @Schema(description = "Number of events rejected due to validation failures")
    private int totalRejected;

    @Schema(description = "Detailed list of responses for individual events")
    private List<TelematicsIngestResponse> results;

    public BatchIngestResponse() {
    }

    public BatchIngestResponse(int totalReceived, int totalProcessed, int totalDuplicatesSkipped, int totalRejected, List<TelematicsIngestResponse> results) {
        this.totalReceived = totalReceived;
        this.totalProcessed = totalProcessed;
        this.totalDuplicatesSkipped = totalDuplicatesSkipped;
        this.totalRejected = totalRejected;
        this.results = results;
    }

    public int getTotalReceived() {
        return totalReceived;
    }

    public void setTotalReceived(int totalReceived) {
        this.totalReceived = totalReceived;
    }

    public int getTotalProcessed() {
        return totalProcessed;
    }

    public void setTotalProcessed(int totalProcessed) {
        this.totalProcessed = totalProcessed;
    }

    public int getTotalDuplicatesSkipped() {
        return totalDuplicatesSkipped;
    }

    public void setTotalDuplicatesSkipped(int totalDuplicatesSkipped) {
        this.totalDuplicatesSkipped = totalDuplicatesSkipped;
    }

    public int getTotalRejected() {
        return totalRejected;
    }

    public void setTotalRejected(int totalRejected) {
        this.totalRejected = totalRejected;
    }

    public List<TelematicsIngestResponse> getResults() {
        return results;
    }

    public void setResults(List<TelematicsIngestResponse> results) {
        this.results = results;
    }
}
