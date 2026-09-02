package org.device.management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.device.management.dto.CreateDeviceRequest;
import org.device.management.dto.DeviceResponse;
import org.device.management.dto.UpdateDeviceRequest;
import org.device.management.entity.Device;
import org.device.management.entity.DeviceState;
import org.device.management.service.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
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
@Tag(
        name="Devices",
        description = "Operations for managing devices"
)
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
    @Operation(
            summary = "Create a device",
            description =
                    """
                    Registers a new device and returns its stored representation.
 
                    `state` is optional; omit it and the device is created as `AVAILABLE`. \
                    `id` and `createdAt` are assigned by the server \
                    """)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Device created",
                    headers =
                    @Header(
                            name = "Location",
                            description = "Absolute URL of the newly created device",
                            schema = @Schema(type = "string", format = "uri")))
    })
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

    @Operation(
            summary = "Update a device (full or partial)",
            description =
                    """
                    Applies the supplied properties to an existing device. Any property omitted \
                    from the body is left unchanged.
 
                    Name and brand are applied before the state change, so rule 2 is evaluated \
                    against the state the device is *stored* in, not the one being requested. \
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device updated"),
            @ApiResponse(
                    responseCode = "404",
                    description = "No device with this id",
                    content =
                    @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(
                    responseCode = "409",
                    description =
                            "Rule 2: the device is IN_USE, so its name or brand cannot be changed",
                    content =
                    @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
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
    @Operation(summary = "Fetch a device by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device found"),
            @ApiResponse(
                    responseCode = "404",
                    description = "No device with this id",
                    content =
                    @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
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
    @Operation(
            summary = "Fetch devices, optionally filtered",
            description =
                    """
                    Returns all devices when no filter is supplied. This single endpoint covers \
                    "fetch all", "fetch by brand" and "fetch by state"; the two filters combine, \
                    so `?brand=Apple&state=IN_USE` returns the intersection.
 
                    Brand matching is case-insensitive but exact — it is not a substring search.
 
                    No match is an empty array with 200, not a 404: the collection exists, it is \
                    simply empty for these criteria.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching devices, possibly empty"),
            @ApiResponse(
                    responseCode = "400",
                    description = "`state` is not one of AVAILABLE, IN_USE, INACTIVE",
                    content =
                    @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public List<DeviceResponse> getAll(@RequestParam(required = false) String brand,
                                       @RequestParam(required = false) DeviceState state) {
        return service.find(brand, state).stream().map(DeviceResponse::response).toList();
    }

    /**
     * Deletes a device
     * @param id device id to delete
     */
    @Operation(
            summary = "Delete a device",
            description =
                    """
                    Removes a device permanently.
 
                    Rule 3: a device that is IN_USE cannot be deleted and returns 409. Release it \
                    first by setting its state to AVAILABLE or INACTIVE, then delete.""")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Device deleted; no body returned"),
            @ApiResponse(
                    responseCode = "404",
                    description = "No device with this id",
                    content =
                    @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(
                    responseCode = "409",
                    description = "Rule 3: the device is IN_USE and cannot be deleted",
                    content =
                    @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
