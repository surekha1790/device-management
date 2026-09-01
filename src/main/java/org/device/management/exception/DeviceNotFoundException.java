package org.device.management.exception;

public class DeviceNotFoundException extends RuntimeException {

    private static final long SerialVersionUID = 1L;

    public DeviceNotFoundException(long id) {
        super("Device %s not found".formatted(id));
    }
}
