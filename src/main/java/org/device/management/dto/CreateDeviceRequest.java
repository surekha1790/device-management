package org.device.management.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.device.management.entity.DeviceState;
import org.jspecify.annotations.Nullable;

public record CreateDeviceRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 120)
        String name,
        @NotBlank(message = "Brand is required")
        @Size(max = 80)
        String brand,
        @Nullable DeviceState state
) {
    public CreateDeviceRequest(String name, String brand) {
       this(name, brand, null);
    }
}

