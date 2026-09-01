package org.device.management.service;

import org.device.management.dto.CreateDeviceRequest;
import org.device.management.dto.UpdateDeviceRequest;
import org.device.management.entity.Device;
import org.device.management.entity.DeviceState;
import org.device.management.exception.DeviceNotFoundException;
import org.device.management.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceService {

    private final DeviceRepository repository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.repository = deviceRepository;
    }

    @Transactional
    public Device create(CreateDeviceRequest request) {
       return repository.save(Device.register(request.name(), request.brand()));
    }

    @Transactional
    public Device update(long id, UpdateDeviceRequest request) {
        Device device = repository.findById(id).orElseThrow(() -> new DeviceNotFoundException(id));
        device.update(request.name(), request.brand(), request.state());
        return device;
    }

    @Transactional(readOnly = true)
    public void findById(long id) {

    }

    @Transactional(readOnly = true)
    public void findAll() {

    }

    @Transactional(readOnly = true)
    public void findByBrand(String brand){

    }

    @Transactional(readOnly = true)
    public void findByState(DeviceState deviceState) {

    }

    @Transactional
    public void delete(long id) {
        Device device = load(id);
        device.validateDeletable();
        repository.delete(device);
    }

    private Device load(long id){
       return repository.findById(id).orElseThrow(() -> new DeviceNotFoundException(id));
    }

}
