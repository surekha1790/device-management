package org.device.management.controller;

import jakarta.validation.Valid;
import org.device.management.dto.CreateDeviceRequest;
import org.device.management.dto.DeviceResponse;
import org.device.management.dto.UpdateDeviceRequest;
import org.device.management.entity.Device;
import org.device.management.entity.DeviceState;
import org.device.management.service.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private static final String GET_DEVICES_URL = "/api/v1/devices/{id}";
    private final DeviceService service;

    public DeviceController(DeviceService deviceService) {
        this.service = deviceService;
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> create(@Valid @RequestBody CreateDeviceRequest deviceRequest,
                                                 UriComponentsBuilder uriBuilder) {
        Device device = service.create(deviceRequest);
        DeviceResponse response = DeviceResponse.response(device);
        URI uri = uriBuilder.path(GET_DEVICES_URL).buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PatchMapping("/{id}")
    public DeviceResponse update(@PathVariable Long id, @Valid @RequestBody UpdateDeviceRequest deviceRequest) {
        Device device = service.update(id, deviceRequest);
        return DeviceResponse.response(device);
    }

    @GetMapping("/{id}")
    public DeviceResponse getById(@PathVariable Long id) {
        return null;
    }

    @GetMapping
    public List<DeviceResponse> getAll(@RequestParam(required = false) String brand,
                                       @RequestParam(required = false) DeviceState state) {
        return null;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

    }
}
