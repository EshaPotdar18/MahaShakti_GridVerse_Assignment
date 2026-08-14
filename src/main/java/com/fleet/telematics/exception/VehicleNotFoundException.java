package com.fleet.telematics.exception;

public class VehicleNotFoundException extends RuntimeException {
    public VehicleNotFoundException(String vehicleId) {
        super("Vehicle device not found: " + vehicleId);
    }
}
