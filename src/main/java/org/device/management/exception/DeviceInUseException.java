package org.device.management.exception;

public class DeviceInUseException extends RuntimeException {

    private static final long SerialVersionUID = 1L;
    private final long deviceId;

    public DeviceInUseException(String message, long deviceId) {
        super(message);
        this.deviceId = deviceId;
    }

    public long getDeviceId(){
        return deviceId;
    }
}
