package org.device.management.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDeviceRequest(

        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "Brand is required")
        String brand
) {
}

