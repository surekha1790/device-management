package org.device.management.dto;

import org.device.management.entity.Device;
import org.device.management.entity.DeviceState;

import java.time.Instant;
import java.util.Objects;

public record DeviceResponse(
        Long id, String name, String brand,
        DeviceState state, Instant createdAt
) {

    public static DeviceResponse response(Device device) {
        return new DeviceResponse(Objects.requireNonNull(device.getId(), "Device Id should be mapped"),
                device.getName(), device.getBrand(), device.getState(), device.getCreatedAt());
    }
}
