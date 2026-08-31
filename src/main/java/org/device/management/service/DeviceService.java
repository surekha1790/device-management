package org.device.management.service;

import org.device.management.dto.CreateDeviceRequest;
import org.device.management.dto.UpdateDeviceRequest;
import org.device.management.entity.DeviceState;
import org.device.management.repository.DeviceRepository;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public void create(CreateDeviceRequest request) {

    }

    public void update(UpdateDeviceRequest request) {

    }

    public void findById(long id) {

    }

    public void findAll() {

    }

    public void findByBrand(String brand){

    }

    public void findByState(DeviceState deviceState) {

    }

    public void delete(long deviceId) {

    }

}
