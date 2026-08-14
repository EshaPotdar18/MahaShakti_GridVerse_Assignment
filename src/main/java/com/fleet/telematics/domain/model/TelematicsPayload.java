package com.fleet.telematics.domain.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class TelematicsPayload {

    @NotNull(message = "speed is required")
    @Min(value = 0, message = "speed cannot be negative")
    @Max(value = 250, message = "speed exceeds maximum threshold of 250 km/h")
    private Double speed;

    @NotNull(message = "fuelLevel is required")
    @Min(value = 0, message = "fuelLevel cannot be negative")
    @Max(value = 100, message = "fuelLevel cannot exceed 100%")
    private Double fuelLevel;

    private Double latitude;
    private Double longitude;
    private String engineStatus;
    private Integer rpm;
    private Double odometer;

    public TelematicsPayload() {
    }

    public TelematicsPayload(Double speed, Double fuelLevel, Double latitude, Double longitude,
                             String engineStatus, Integer rpm, Double odometer) {
        this.speed = speed;
        this.fuelLevel = fuelLevel;
        this.latitude = latitude;
        this.longitude = longitude;
        this.engineStatus = engineStatus;
        this.rpm = rpm;
        this.odometer = odometer;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(Double fuelLevel) {
        this.fuelLevel = fuelLevel;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getEngineStatus() {
        return engineStatus;
    }

    public void setEngineStatus(String engineStatus) {
        this.engineStatus = engineStatus;
    }

    public Integer getRpm() {
        return rpm;
    }

    public void setRpm(Integer rpm) {
        this.rpm = rpm;
    }

    public Double getOdometer() {
        return odometer;
    }

    public void setOdometer(Double odometer) {
        this.odometer = odometer;
    }
}
