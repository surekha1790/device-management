package org.device.management.dto;


import jakarta.validation.constraints.NotBlank;
import org.device.management.entity.DeviceState;
import org.jspecify.annotations.Nullable;

public record CreateDeviceRequest(

        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "Brand is required")
        String brand,
        @Nullable DeviceState state
) {
    public CreateDeviceRequest(String name, String brand) {
       this(name, brand, null);
    }
}

