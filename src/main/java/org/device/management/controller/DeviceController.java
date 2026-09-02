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

/**
 * REST API endpoints for devices
 *
 * Register new device, update name, brand, and state
 * Fetch device by id, brand, state and all devices
 * delete device by id
 */

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private static final String GET_DEVICES_URL = "/api/v1/devices/{id}";
    private final DeviceService service;

    public DeviceController(DeviceService deviceService) {
        this.service = deviceService;
    }

    /**
     * Register a new device
     * @param deviceRequest name, brand and optional device state
     * @return {@code 201 created} with saved device along with {@code location} header
     */
    @PostMapping
    public ResponseEntity<DeviceResponse> create(@Valid @RequestBody CreateDeviceRequest deviceRequest,
                                                 UriComponentsBuilder uriBuilder) {
        Device device = service.create(deviceRequest);
        DeviceResponse response = DeviceResponse.response(device);
        URI uri = uriBuilder.path(GET_DEVICES_URL).buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    /**
     * Perform full or partial update on existing device
     * While updating, it checks the device status and does not allow if device is in use
     * @param id existing device id to update
     * @param deviceRequest properties to update, ignored properties are unchanged.
     * @return updated device details
     */
    @PatchMapping("/{id}")
    public DeviceResponse update(@PathVariable Long id, @Valid @RequestBody UpdateDeviceRequest deviceRequest) {
        Device device = service.update(id, deviceRequest);
        return DeviceResponse.response(device);
    }

    /**
     * Fetches a single device
     * @param id device id to fetch
     * @return device retrieved by id
     */
    @GetMapping("/{id}")
    public DeviceResponse getById(@PathVariable Long id) {
        return DeviceResponse.response(service.findById(id));
    }

    /**
     * Fetches all or devices by brand, state
     * @param brand fetch by brand-case insensitive, omit to fetch all
     * @param state fetch by state, omit to fetch all
     * @return all or matching devices, empty when no match
     */
    @GetMapping
    public List<DeviceResponse> getAll(@RequestParam(required = false) String brand,
                                       @RequestParam(required = false) DeviceState state) {
        return service.find(brand, state).stream().map(DeviceResponse::response).toList();
    }

    /**
     * Deletes a device
     * @param id device id to delete
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
