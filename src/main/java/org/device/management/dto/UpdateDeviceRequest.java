package org.device.management.dto;

import org.device.management.entity.DeviceState;

public record UpdateDeviceRequest (
        String name,
        String brand,
        DeviceState state
) {
}
