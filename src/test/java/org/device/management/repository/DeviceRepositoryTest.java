package org.device.management.repository;

import jakarta.persistence.EntityManager;
import org.device.management.entity.Device;
import org.device.management.entity.DeviceState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
public class DeviceRepositoryTest {

    @Autowired
    private DeviceRepository repository;
    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void clean() {
        repository.deleteAll();
        entityManager.flush();
    }

    @Test
    @DisplayName("save and fetch device")
    public void saveAndFetch() {
        Device device = repository.saveAndFlush(Device.register("Iphone Max", "Apple", DeviceState.AVAILABLE));
        Long id = device.getId();
        entityManager.clear();

        Device loadedDevice = repository.findById(id).orElseThrow();
        assertAll(() -> assertThat(loadedDevice.getName()).isEqualTo("Iphone Max"),
                () -> assertThat(loadedDevice.getBrand()).isEqualTo("Apple"),
                () -> assertThat(loadedDevice.getState()).isEqualTo(DeviceState.AVAILABLE));
    }

    @Test
    @DisplayName("check stored state enum value")
    public void checkState() {
        Device device = repository.saveAndFlush(Device.register("Iphone Max", "Apple", DeviceState.AVAILABLE));
        Long id = device.getId();
        Object d = entityManager.createNativeQuery("Select state from devices where id=:id")
                .setParameter("id", id)
                .getSingleResult();

        assertThat(d).hasToString("AVAILABLE");
    }

    @Test
    @DisplayName("version increments on each update")
    public void versionIncrement() {
        Device device = repository.saveAndFlush(Device.register("Iphone Max", "Apple", DeviceState.AVAILABLE));
        Long version = device.getVersion();
        device.update("Iphone Max 2", null, null);
        repository.saveAndFlush(device);
        assertThat(device.getVersion()).isGreaterThan(version);
    }

    @Test
    @DisplayName("brand with ignoreCase")
    public void findBrandIgnoreCase() {
        repository.saveAndFlush(Device.register("Iphone Max", "Apple", DeviceState.AVAILABLE));
        entityManager.clear();
        assertThat(repository.findByBrandIgnoreCase("apple").size()).isEqualTo(1);
    }

    @Test
    @DisplayName("find device by state")
    public void findByState() {
        repository.saveAndFlush(Device.register("Iphone Max", "Apple", DeviceState.AVAILABLE));
        entityManager.clear();
        assertThat(repository.findByState(DeviceState.AVAILABLE).size()).isEqualTo(1);
    }

    @Test
    @DisplayName("find device by brand and state")
    public void findByBrandAndState() {
        repository.saveAndFlush(Device.register("Iphone Max", "Apple", DeviceState.AVAILABLE));
        entityManager.clear();
        assertThat(repository.findByBrandIgnoreCaseAndState("Apple", DeviceState.AVAILABLE).size()).isEqualTo(1);
    }

}
