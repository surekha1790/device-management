package org.device.management.service;

import org.device.management.dto.CreateDeviceRequest;
import org.device.management.dto.UpdateDeviceRequest;
import org.device.management.entity.Device;
import org.device.management.entity.DeviceState;
import org.device.management.exception.DeviceNotFoundException;
import org.device.management.repository.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeviceService {

    private static final Logger log = LoggerFactory.getLogger(DeviceService.class);

    private final DeviceRepository repository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.repository = deviceRepository;
    }

    @Transactional
    public Device create(CreateDeviceRequest request) {
        log.info("Creating new device with name={}, brand={}", request.name(), request.brand());
        if(request.state() == null) {
            return repository.save(Device.register(request.name(), request.brand()));
        }
        return repository.save(Device.register(request.name(), request.brand(), request.state()));
    }

    @Transactional
    public Device update(long id, UpdateDeviceRequest request) {
        Device device = load(id);
        device.update(request.name(), request.brand(), request.state());
        log.info("Updated the device id={} ", id);
        return device;
    }

    @Transactional(readOnly = true)
    public Device findById(long id) {
        return load(id);
    }

    @Transactional(readOnly = true)
    public List<Device> find(String brand, DeviceState state) {
        if(brand != null && state != null) {
            return repository.findByBrandIgnoreCaseAndState(brand,state);
        }
        if(brand != null){
            return repository.findByBrandIgnoreCase(brand);
        }
        if(state != null) {
            return repository.findByState(state);
        }
        return repository.findAll();
    }

    @Transactional
    public void delete(long id) {
        Device device = load(id);
        device.validateDeletable();
        repository.delete(device);
        log.info("Deleted the device id={}", id);
    }

    private Device load(long id){
       return repository.findById(id).orElseThrow(() -> new DeviceNotFoundException(id));
    }

}
