package org.device.management.controller;

import org.device.management.dto.CreateDeviceRequest;
import org.device.management.dto.UpdateDeviceRequest;
import org.device.management.entity.Device;
import org.device.management.entity.DeviceState;
import org.device.management.exception.DeviceInUseException;
import org.device.management.exception.DeviceNotFoundException;
import org.device.management.service.DeviceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
public class DeviceControllerTest {

    private static final long ID = 123L;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DeviceService service;


    @Test
    @DisplayName("POST: create new device should be success")
    public void shouldCreateDevice() throws Exception {
        CreateDeviceRequest request = new CreateDeviceRequest("Iphone 17 Pro", "Apple");
        when(service.create(any(CreateDeviceRequest.class)))
                .thenReturn(createWithId(ID, request.name(), request.brand(), DeviceState.AVAILABLE));
        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/devices/123"))
                .andExpect(jsonPath("$.id").value(123))
                .andExpect(jsonPath("$.name").value("Iphone 17 Pro"))
                .andExpect(jsonPath("$.brand").value("Apple"))
                .andExpect(jsonPath("$.state").value("AVAILABLE"));
    }

    @Test
    @DisplayName("POST: when input contains additional spaces, create new device should be success")
    public void shouldCreateDevice_inputWithAdditionalSpaces() throws Exception {
        CreateDeviceRequest request = new CreateDeviceRequest("  Iphone 17 Pro  ", "  Apple  ");
        when(service.create(any(CreateDeviceRequest.class)))
                .thenReturn(createWithId(ID, request.name(), request.brand(), DeviceState.AVAILABLE));
        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/devices/123"))
                .andExpect(jsonPath("$.id").value(ID))
                .andExpect(jsonPath("$.name").value("Iphone 17 Pro"))
                .andExpect(jsonPath("$.brand").value("Apple"))
                .andExpect(jsonPath("$.state").value("AVAILABLE"));
    }

    @Test
    @DisplayName("POST: when device name is blank, rest call should fail")
    public void blankNameFailCreation() throws Exception {
        CreateDeviceRequest request = new CreateDeviceRequest(" ", "  Apple  ");
        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(service, never()).create(any(CreateDeviceRequest.class));
    }

    @Test
    @DisplayName("POST: when device brand name is blank, rest call should fail")
    public void blankBrandNameFailCreation() throws Exception {
        CreateDeviceRequest request = new CreateDeviceRequest("Iphone 17 Pro ", " ");
        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(service, never()).create(any(CreateDeviceRequest.class));
    }

    @Test
    @DisplayName("POST: create device along with state is success")
    public void createDeviceWithState() throws Exception {
        CreateDeviceRequest request = new CreateDeviceRequest("Iphone 17 Pro ", "Apple", DeviceState.INACTIVE);
        when(service.create(request))
                .thenReturn(createWithId(ID, request.name(), request.brand(), DeviceState.INACTIVE));

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "http://localhost/api/v1/devices/123"))
                .andExpect(jsonPath("$.id").value(ID))
                .andExpect(jsonPath("$.name").value("Iphone 17 Pro"))
                .andExpect(jsonPath("$.brand").value("Apple"))
                .andExpect(jsonPath("$.state").value("INACTIVE"));
    }

    @Test
    @DisplayName("POST: create device with invalid state should fail")
    public void createDeviceWithInvalidState() throws Exception {
        mockMvc.perform(post("/api/v1/devices")
                        .content("""
                                {"name":"Samsung 26", "brand":"Samsung", "state":"TEST"}""")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(service, never()).create(any(CreateDeviceRequest.class));
    }

    @Test
    @DisplayName("PATCH: update device name should be success")
    public void updateDeviceNameSuccess() throws Exception {
        UpdateDeviceRequest request = new UpdateDeviceRequest("Iphone 17", null, null);
        Device device = createWithId(ID, request.name(), "Apple", DeviceState.AVAILABLE);
        when(service.update(ID, request)).thenReturn(device);
        mockMvc.perform(patch("/api/v1/devices/123")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID))
                .andExpect(jsonPath("$.name").value("Iphone 17"))
                .andExpect(jsonPath("$.brand").value("Apple"))
                .andExpect(jsonPath("$.state").value("AVAILABLE"));

    }

    @Test
    @DisplayName("PATCH: update device brand name should be success")
    public void updateDeviceBrandNameSuccess() throws Exception {
        UpdateDeviceRequest request = new UpdateDeviceRequest(null, "IOS", null);
        Device device = createWithId(ID, "Iphone 17", request.brand(), DeviceState.AVAILABLE);
        when(service.update(ID, request)).thenReturn(device);
        mockMvc.perform(patch("/api/v1/devices/123")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID))
                .andExpect(jsonPath("$.name").value("Iphone 17"))
                .andExpect(jsonPath("$.brand").value("IOS"))
                .andExpect(jsonPath("$.state").value("AVAILABLE"));

    }

    @Test
    @DisplayName("PATCH: update device state should be success")
    public void updateDeviceStateSuccess() throws Exception {
        UpdateDeviceRequest request = new UpdateDeviceRequest(null, null, DeviceState.IN_USE);
        Device device = createWithId(ID, "Iphone 17", "Apple", request.state());
        when(service.update(ID, request)).thenReturn(device);
        mockMvc.perform(patch("/api/v1/devices/123")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID))
                .andExpect(jsonPath("$.name").value("Iphone 17"))
                .andExpect(jsonPath("$.brand").value("Apple"))
                .andExpect(jsonPath("$.state").value("IN_USE"));

    }

    @Test
    @DisplayName("PATCH: update in use device name should fail")
    public void updateInUseDeviceNameFails() throws Exception {
        UpdateDeviceRequest request = new UpdateDeviceRequest("Iphone 17", null, null);
        when(service.update(ID, request)).thenThrow(new DeviceInUseException("Device 123 is in use, can not be updated", 123L));

        mockMvc.perform(patch("/api/v1/devices/123")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.deviceId").value(ID))
                .andExpect(jsonPath("$.title").value("Device is in use"));

    }

    @Test
    @DisplayName("PATCH: update in use device brand should fail")
    public void updateInUseDeviceBrandFails() throws Exception {
        UpdateDeviceRequest request = new UpdateDeviceRequest(null, "IOS", null);
        when(service.update(ID, request)).thenThrow(new DeviceInUseException("Device 123 is in use, can not be updated", 123L));

        mockMvc.perform(patch("/api/v1/devices/123")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.deviceId").value(ID))
                .andExpect(jsonPath("$.title").value("Device is in use"));

    }

    @Test
    @DisplayName("PATCH: update invalid device state should fail")
    public void updateInvalidDeviceStateFai() throws Exception {
        mockMvc.perform(patch("/api/v1/devices/123")
                        .content("""
                                {"name":"Samsung 26", "brand":"Samsung", "state":"TEST"}""")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(service, never()).update(anyLong(), any(UpdateDeviceRequest.class));

    }

    @Test
    @DisplayName("GET device by id success")
    public void findById() throws Exception {
        when(service.findById(anyLong())).thenReturn(
                createWithId(345L, "Pixel 6", "Google", DeviceState.IN_USE));
        mockMvc.perform(get("/api/v1/devices/345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(345))
                .andExpect(jsonPath("$.name").value("Pixel 6"))
                .andExpect(jsonPath("$.brand").value("Google"));

    }

    @Test
    @DisplayName("GET returns device not found when no device with id")
    public void findByIdNotFound() throws Exception {
        when(service.findById(anyLong())).thenThrow(new DeviceNotFoundException(345));
        mockMvc.perform(get("/api/v1/devices/345"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Device Not Found"));
    }


    @Test
    @DisplayName("GET all devices returns list")
    public void findAllDevices() throws Exception {
        when(service.find(null, null)).thenReturn(
                List.of(createWithId(345L, "Pixel 6", "Google", DeviceState.IN_USE)));
        mockMvc.perform(get("/api/v1/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(345))
                .andExpect(jsonPath("$[0].name").value("Pixel 6"))
                .andExpect(jsonPath("$[0].brand").value("Google"))
                .andExpect(jsonPath("$[0].state").value("IN_USE"));

    }

    @Test
    @DisplayName("GET devices by brand returns list")
    public void findByBrand() throws Exception {
        when(service.find("Samsung", null)).thenReturn(
                List.of(createWithId(346L, "Galaxy S26", "Samsung", DeviceState.IN_USE)));
        mockMvc.perform(get("/api/v1/devices?brand=Samsung"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(346))
                .andExpect(jsonPath("$[0].name").value("Galaxy S26"))
                .andExpect(jsonPath("$[0].brand").value("Samsung"))
                .andExpect(jsonPath("$[0].state").value("IN_USE"));

    }

    @Test
    @DisplayName("GET devices by brand ignoreCase returns list")
    public void findByBrandIgnoreCase() throws Exception {
        when(service.find("samsung", null)).thenReturn(
                List.of(createWithId(346L, "Galaxy S26", "Samsung", DeviceState.IN_USE)));
        mockMvc.perform(get("/api/v1/devices?brand=samsung"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(346))
                .andExpect(jsonPath("$[0].name").value("Galaxy S26"))
                .andExpect(jsonPath("$[0].brand").value("Samsung"))
                .andExpect(jsonPath("$[0].state").value("IN_USE"));

    }

    @Test
    @DisplayName("GET devices by state returns list")
    public void findByState() throws Exception {
        when(service.find(null, DeviceState.IN_USE)).thenReturn(
                List.of(createWithId(346L, "Galaxy S26", "Samsung", DeviceState.IN_USE)));
        mockMvc.perform(get("/api/v1/devices?state=IN_USE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(346))
                .andExpect(jsonPath("$[0].name").value("Galaxy S26"))
                .andExpect(jsonPath("$[0].brand").value("Samsung"))
                .andExpect(jsonPath("$[0].state").value("IN_USE"));

    }

    @Test
    @DisplayName("GET devices by brand and state returns list")
    public void findByBrandAndState() throws Exception {
        when(service.find("samsung", DeviceState.IN_USE)).thenReturn(
                List.of(createWithId(346L, "Galaxy S26", "Samsung", DeviceState.IN_USE)));
        mockMvc.perform(get("/api/v1/devices?brand=samsung&state=IN_USE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(346))
                .andExpect(jsonPath("$[0].name").value("Galaxy S26"))
                .andExpect(jsonPath("$[0].brand").value("Samsung"))
                .andExpect(jsonPath("$[0].state").value("IN_USE"));

    }

    @Test
    @DisplayName("DELETE: delete device by id should be success")
    public void deleteById() throws Exception {
        mockMvc.perform(delete("/api/v1/devices/345"))
                .andExpect(status().isNoContent());
        verify(service).delete(345);
    }

    @Test
    @DisplayName("DELETE: delete in use device throws exception")
    public void deleteInUseDeviceFail() throws Exception {
        doThrow(new DeviceInUseException("Device is in use, can not be deleted", 345))
                .when(service).delete(345);
        mockMvc.perform(delete("/api/v1/devices/345"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("DELETE: delete non existing device")
    public void deleteNotFound() throws Exception {
        doThrow(new DeviceNotFoundException(345))
                .when(service).delete(345);
        mockMvc.perform(delete("/api/v1/devices/345"))
                .andExpect(status().isNotFound());
    }

    private Device createWithId(long id, String name, String brand, DeviceState state) {
        Device device = Device.register(name, brand, state);
        ReflectionTestUtils.setField(device, "id", id);
        return device;
    }
}
