package org.device.management.entity;

import org.device.management.exception.DeviceInUseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceTest {

    private static final Instant CREATED_AT = Instant.parse("2026-09-02T00:00:00z");

    @Test
    @DisplayName("creation time can not be update")
    public void noUpdateOnCreationTime() {
        Device device = registerDevice();
        device.update("Iphone Max Pro", null, null);
        assertEquals(CREATED_AT, device.getCreatedAt());
    }

    @Test
    @DisplayName("update name on in use device rejected")
    public void updateInUseDeviceName() {
        Device device = Device.register("Iphone Max", "Apple", DeviceState.IN_USE);
        ReflectionTestUtils.setField(device, "id", 456L);
        assertThrows(DeviceInUseException.class, () -> device.update("Iphone Max PRO", null, null));
    }

    @Test
    @DisplayName("update brand on in use device rejected")
    public void updateInUseDeviceBrand() {
        Device device = Device.register("Iphone Max", "Apple", DeviceState.IN_USE);
        ReflectionTestUtils.setField(device, "id", 456L);
        assertThrows(DeviceInUseException.class, () -> device.update(null, "IOS", null));
    }

    @Test
    @DisplayName("delete in use device rejected")
    public void deleteInUseDeviceRejected() {
        Device device = Device.register("Iphone Max", "Apple", DeviceState.IN_USE);
        ReflectionTestUtils.setField(device, "id", 456L);
        assertThrows(DeviceInUseException.class, device::validateDeletable);
    }

    private Device registerDevice() {
        return Device.register("Iphone Max", "Apple", DeviceState.AVAILABLE, CREATED_AT);
    }
}
