package org.device.management.dto;

import org.device.management.entity.DeviceState;

import java.time.Instant;

public record DeviceResponse(
        Long id, String name, String brand,
        DeviceState state, Instant createdAt
) {
}
