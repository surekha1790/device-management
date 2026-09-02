package org.device.management.service;

import org.device.management.dto.CreateDeviceRequest;
import org.device.management.dto.UpdateDeviceRequest;
import org.device.management.entity.Device;
import org.device.management.entity.DeviceState;
import org.device.management.exception.DeviceInUseException;
import org.device.management.exception.DeviceNotFoundException;
import org.device.management.repository.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Provides business operations for registering and managing device
 * data persist through {@link DeviceRepository}
 */
@Service
public class DeviceService {

    private static final Logger log = LoggerFactory.getLogger(DeviceService.class);

    private final DeviceRepository repository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.repository = deviceRepository;
    }

    /**
     * Register a new device
     * state is optional and default to AVAILABLE
     * @param request device details
     * @return created device
     */
    @Transactional
    public Device create(CreateDeviceRequest request) {
        log.info("Creating new device with name={}, brand={}", request.name(), request.brand());
        if(request.state() == null) {
            return repository.save(Device.register(request.name(), request.brand()));
        }
        return repository.save(Device.register(request.name(), request.brand(), request.state()));
    }

    /**
     * Update an existing device. Full or Partial and ignored properties are unchanged.
     * Domain rule is, do not allow updating a device which is {@link DeviceState#IN_USE}
     * @param id device id to update
     * @param request device details to update
     * @return updated device details
     * @throws DeviceNotFoundException if not device exists with id
     * @throws DeviceInUseException if trying to update {@link DeviceState#IN_USE} device
     */
    @Transactional
    public Device update(long id, UpdateDeviceRequest request) {
        Device device = load(id);
        device.update(request.name(), request.brand(), request.state());
        log.info("Updated the device id={} ", id);
        return device;
    }

    /**
     * Fetched device by identifier
     * @param id device id
     * @return matching device
     * @throws DeviceNotFoundException if no device exists with id
     */
    @Transactional(readOnly = true)
    public Device findById(long id) {
        return load(id);
    }

    /**
     * find all devices or with filter on brand, state
     * @param brand device brand to filter
     * @param state device state to filter
     * @return List of devices matching with filter or all
     */
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

    /**
     * Deletes a device
     * @param id identifier of the device
     * @throws DeviceNotFoundException if no device exists with id
     * @throws DeviceInUseException if device is {@link DeviceState#IN_USE}
     */
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
