package org.device.management.service;

import org.device.management.dto.CreateDeviceRequest;
import org.device.management.dto.UpdateDeviceRequest;
import org.device.management.entity.Device;
import org.device.management.entity.DeviceState;
import org.device.management.exception.DeviceInUseException;
import org.device.management.exception.DeviceNotFoundException;
import org.device.management.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeviceServiceTest {

    @Mock
    private DeviceRepository repository;
    private DeviceService service;

    @BeforeEach
    public void setup() {
        service = new DeviceService(repository);
    }

    @Test
    @DisplayName("create new device")
    public void create() {
        CreateDeviceRequest request = new CreateDeviceRequest("Pixel 6", "Google");
        when(repository.save(any(Device.class))).thenAnswer(inv -> inv.getArgument(0));
        Device device = service.create(request);
        assertNotNull(device);
        assertEquals("Pixel 6", device.getName());
        assertEquals("Google", device.getBrand());
        assertEquals(DeviceState.AVAILABLE, device.getState());
    }

    @Test
    @DisplayName("create new device with state")
    public void createWithState() {
        CreateDeviceRequest request = new CreateDeviceRequest("Pixel 6", "Google", DeviceState.INACTIVE);
        when(repository.save(any(Device.class))).thenAnswer(inv -> inv.getArgument(0));
        Device device = service.create(request);
        assertDevice(device, "Pixel 6", "Google", DeviceState.INACTIVE);
    }

    @Test
    @DisplayName("update device name")
    public void updateName() {
        UpdateDeviceRequest request = new UpdateDeviceRequest("Pixel 7", null, null);
        when(repository.findById(anyLong())).thenReturn(
                Optional.of(Device.register("Pixel 6", "Google", DeviceState.AVAILABLE)));
        Device device = service.update(123, request);
        assertDevice(device, "Pixel 7", "Google", DeviceState.AVAILABLE);
    }

    @Test
    @DisplayName("update in use device name should fail")
    public void updateInUseDeviceName() {
        UpdateDeviceRequest request = new UpdateDeviceRequest("Pixel 8", null, null);
        when(repository.findById(anyLong())).thenReturn(
                Optional.of(createWithId(125, "Pixel 7", "Google", DeviceState.IN_USE)));
        assertThrows(DeviceInUseException.class, () -> service.update(125, request),
                "Device 125 is in use, can not be updated");
    }

    @Test
    @DisplayName("update device brand")
    public void updateBrand() {
        UpdateDeviceRequest request = new UpdateDeviceRequest(null, "Google N", null);
        when(repository.findById(anyLong())).thenReturn(
                Optional.of(Device.register("Pixel 7", "Google", DeviceState.AVAILABLE)));
        Device device = service.update(123, request);
        assertDevice(device, "Pixel 7", "Google N", DeviceState.AVAILABLE);
    }

    @Test
    @DisplayName("update in use device brand should fail")
    public void updateInUseDeviceBrand() {
        UpdateDeviceRequest request = new UpdateDeviceRequest(null, "Google1", null);
        when(repository.findById(anyLong())).thenReturn(
                Optional.of(createWithId(125, "Pixel 7", "Google", DeviceState.IN_USE)));
        assertThrows(DeviceInUseException.class, () -> service.update(125, request),
                "Device 125 is in use, can not be updated");
    }

    @Test
    @DisplayName("update device state")
    public void updateState() {
        UpdateDeviceRequest request = new UpdateDeviceRequest(null, null, DeviceState.INACTIVE);
        when(repository.findById(anyLong())).thenReturn(
                Optional.of(Device.register("Pixel 7", "Google", DeviceState.AVAILABLE)));
        Device device = service.update(123, request);
        assertDevice(device, "Pixel 7", "Google", DeviceState.INACTIVE);
    }

    @Test
    @DisplayName("find device by id")
    public void findById() {
        when(repository.findById(anyLong())).thenReturn(
                Optional.of(Device.register("Pixel 7", "Google", DeviceState.AVAILABLE)));
        Device device = service.findById(123);
        assertDevice(device, "Pixel 7", "Google", DeviceState.AVAILABLE);
    }

    @Test
    @DisplayName("device not found with id")
    public void notFoundById() {
        assertThrows(DeviceNotFoundException.class, () -> service.findById(123),
                "Device 123 not found");
    }

    @Test
    @DisplayName("find device by brand")
    public void findByBrand() {
        when(repository.findByBrandIgnoreCase("Samsung")).thenReturn(
                List.of(Device.register("Galaxy 27", "Samsung", DeviceState.AVAILABLE)));
        List<Device> devices = service.find("Samsung", null);
        assertFalse(devices.isEmpty());
        assertEquals(1, devices.size());
       assertDevice(devices.getFirst(), "Galaxy 27", "Samsung", DeviceState.AVAILABLE);
    }

    @Test
    @DisplayName("find device by state")
    public void findByState() {
        when(repository.findByState(DeviceState.IN_USE)).thenReturn(
                List.of(Device.register("Galaxy 27", "Samsung", DeviceState.IN_USE)));
        List<Device> devices = service.find(null, DeviceState.IN_USE);
        assertFalse(devices.isEmpty());
        assertEquals(1, devices.size());
        assertDevice(devices.getFirst(), "Galaxy 27", "Samsung", DeviceState.IN_USE);
    }

    @Test
    @DisplayName("find device by brand and state")
    public void findByBrandAndState() {
        when(repository.findByBrandIgnoreCaseAndState("Google", DeviceState.IN_USE)).thenReturn(
                List.of(Device.register("Pixel 7", "Google", DeviceState.IN_USE)));
        List<Device> devices = service.find("Google", DeviceState.IN_USE);
        assertFalse(devices.isEmpty());
        assertEquals(1, devices.size());
        assertDevice(devices.getFirst(), "Pixel 7", "Google", DeviceState.IN_USE);
    }

    @Test
    @DisplayName("find all devices")
    public void findAll() {
        when(repository.findAll()).thenReturn(
                List.of(Device.register("Pixel 7", "Google", DeviceState.IN_USE)));
        List<Device> devices = service.find(null, null);
        assertFalse(devices.isEmpty());
        assertEquals(1, devices.size());
        assertDevice(devices.getFirst(), "Pixel 7", "Google", DeviceState.IN_USE);
    }

    @Test
    @DisplayName("delete device by id")
    public void deleteById() {
        Device device = Device.register("Pixel 7", "Google", DeviceState.AVAILABLE);
        when(repository.findById(anyLong())).thenReturn(
                Optional.of(device));
        service.delete(123);
        verify(repository).delete(device);
    }

    @Test
    @DisplayName("device not found to delete")
    public void deviceNotFound() {
        when(repository.findById(anyLong())).thenThrow(DeviceNotFoundException.class);
        assertThrows(DeviceNotFoundException.class, () -> service.delete(123));
    }

    @Test
    @DisplayName("delete in use device")
    public void inUseDeviceDelete() {
        Device device = createWithId(123, "Pixel 7", "Google", DeviceState.IN_USE);
        when(repository.findById(anyLong())).thenReturn(
                Optional.of(device));
        assertThrows(DeviceInUseException.class, () -> service.delete(123),
                "Device 123 is in use, can not be deleted");

    }

    private void assertDevice(Device device, String name, String brand, DeviceState state){
        assertNotNull(device);
        assertEquals(name, device.getName());
        assertEquals(brand, device.getBrand());
        assertEquals(state, device.getState());
    }

    private Device createWithId(long id, String name, String brand, DeviceState state) {
        Device device = Device.register(name, brand, state);
        ReflectionTestUtils.setField(device, "id", id);
        return device;
    }
}
