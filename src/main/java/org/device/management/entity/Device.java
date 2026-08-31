package org.device.management.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "device",
        indexes = {@Index(name="idx_device_brand", columnList = "brand")})
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
}
