package org.device.management.controller;

import jakarta.validation.Valid;
import org.device.management.dto.CreateDeviceRequest;
import org.device.management.dto.DeviceResponse;
import org.device.management.service.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/device")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ResponseEntity<DeviceResponse> create(@Valid @RequestBody CreateDeviceRequest deviceRequest) {

        return null;
    }

    @PatchMapping("{id}")
    public DeviceResponse update(@PathVariable Long id, @RequestBody CreateDeviceRequest deviceRequest){
        return null;
    }

    @GetMapping("{id}")
    public DeviceResponse getById(@PathVariable Long id) {
        return null;
    }

    @GetMapping
    public List<DeviceResponse> getAll(){
        return null;
    }

    @GetMapping
    public List<DeviceResponse> getByBrand() {
        return null;
    }

    @GetMapping
    public List<DeviceResponse> getByState() {
        return null;
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

    }
}
