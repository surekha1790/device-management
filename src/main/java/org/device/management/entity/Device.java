package org.device.management.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.device.management.exception.DeviceInUseException;

import java.time.Instant;
import java.util.Objects;

@Getter
@Entity
@Table(name = "devices",
        indexes = {@Index(name = "idx_device_brand", columnList = "brand")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "device_seq")
    @SequenceGenerator(name = "device_seq", sequenceName = "device_seq")
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 80)
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceState state;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @Version
    private long version;

    private Device(String name, String brand, DeviceState deviceState, Instant createdAt) {
        this.name = validateText(name, "Name");
        this.brand = validateText(brand, "Brand");
        this.state = deviceState;
        this.createdAt = createdAt;
    }

    public static Device register(String name, String brand, DeviceState state) {
        return new Device(name, brand, state, Instant.now());
    }

    public static Device register(String name, String brand) {
        return new Device(name, brand, DeviceState.AVAILABLE, Instant.now());
    }

    public static Device register(String name, String brand, DeviceState state, Instant createdAt) {
        return new Device(name, brand, state, createdAt);
    }

    public void update(String name, String brand, DeviceState state) {
        if (name != null) {
            rename(name);
        }
        if (brand != null) {
            updateBrand(brand);
        }
        if (state != null) {
            this.state = state;
        }
    }

    private void rename(String name) {
        String value = validateText(name, "Name");
        validateState();
        if (value.equals(this.name))
            return;
        this.name = value;
    }

    private void updateBrand(String brand) {
        String value = validateText(brand, "Brand");
        validateState();
        if (value.equals(this.brand))
            return;
        this.brand = value;
    }

    public void validateDeletable() {
        if(isInUse()) {
            throw new DeviceInUseException("Device %s is in use, can not be deleted".formatted(id), id);
        }
    }

    private void validateState() {
        if (isInUse())
            throw new DeviceInUseException("Device %s is in use, can not be updated".formatted(id), id);
    }

    private boolean isInUse() {
        return DeviceState.IN_USE.equals(state);
    }

    private static String validateText(String value, String name) {
        Objects.requireNonNull(value, () -> name + "should not be null");
        String trimmedValue = value.strip();
        if (trimmedValue.isBlank()) {
            throw new IllegalArgumentException(name + "should not be blank");
        }
        return trimmedValue;
    }
}
