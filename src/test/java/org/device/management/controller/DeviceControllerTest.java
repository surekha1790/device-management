package org.device.management.controller;

import org.device.management.dto.CreateDeviceRequest;
import org.device.management.dto.UpdateDeviceRequest;
import org.device.management.entity.Device;
import org.device.management.entity.DeviceState;
import org.device.management.exception.DeviceInUseException;
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


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                .thenReturn(createWithId(request.name(), request.brand(), DeviceState.AVAILABLE));
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
                .thenReturn(createWithId(request.name(), request.brand(), DeviceState.AVAILABLE));
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
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

/*    @Test
    @DisplayName("POST: when device state is invalid, rest call should fail")
    public void invalidStateFailCreation() throws Exception {
        CreateDeviceRequest request = new CreateDeviceRequest("Iphone 17 Pro ", " ");
        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(service, never()).create(any(CreateDeviceRequest.class));
    }*/

    @Test
    @DisplayName("PATCH: update device name should be success")
    public void updateDeviceNameSuccess() throws Exception {
        UpdateDeviceRequest request = new UpdateDeviceRequest("Iphone 17", null, null);
        Device device = createWithId(request.name(), "Apple", DeviceState.AVAILABLE);
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
        Device device = createWithId("Iphone 17", request.brand(), DeviceState.AVAILABLE);
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
        Device device = createWithId("Iphone 17", "Apple", request.state());
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

    private Device createWithId(String name, String brand, DeviceState state) {
        Device device = Device.register(name, brand, state);
        ReflectionTestUtils.setField(device, "id", ID);
        return device;
    }
}
