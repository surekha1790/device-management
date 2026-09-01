CREATE SEQUENCE device_seq
START WITH 1
INCREMENT BY 50;

CREATE TABLE devices (
    id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    brand VARCHAR(100) NOT NULL,
    state VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_devices PRIMARY KEY (id)
);

CREATE INDEX idx_device_brand ON devices(brand);
