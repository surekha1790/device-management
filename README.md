# Devices API

### REST API capable of persisting and managing device resources.

#### Tech stack: Java 21 · Spring Boot 4 · Spring Data JPA · PostgreSQL · Flyway · Docker

### Task

develop a REST API capable of persisting and managing device resources.

### Domain

| Name          | Description                               |
|---------------|-------------------------------------------|
| Id            | DB generated                              |
| Name          | Device Name                               |
| Brand         | Device Brand                              |
| State         | Fixed Values (available, in-use, inac:ve) |
| Creation Time | Device registered time                    |

**Required Functionality**

* Create a new device.
* Fully and/or partially update an existing device.
* Fetch a single device.
* Fetch all devices.
* Fetch devices by brand.
* Fetch devices by state.
* Delete a single device.

**Domain Validations**

1. Creation time cannot be updated.
2. Name and brand properties cannot be updated if the device is in use.
3. In use devices cannot be deleted.

-------

## Run with Docker

**With Docker**

```bash
    docker compose up --build
   
   # login to db in container
    docker exec -it device-db psql -U device -d device_management
   # logs
    docker logs -f device-api
```

This alone brings up PostgreSQL and application. Flyway creates the schema on first start.

Server: `http://localhost:8080`
PostgreSQL: localhost:5432

Profile and Connection properties come from external environment variables with default values.

`SPRING_PROFILES_ACTIVE, APP_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD`

Default value is set to dev for SPRING_PROFILES_ACTIVE to bring up easily. 

**Override while running**
```shell
    # Bash
    SPRING_PROFILES_ACTIVE=dev && docker compose up --build
    # Windows CMD
    set SPRING_PROFILES_ACTIVE=dev && docker compose up --build
```
----

### Swagger and Documentation
Documentation is available at
Swagger-UI: http://localhost:8080/swagger-ui/index.html
Spec: http://localhost:8080/v3/api-docs


## API

Base path is `/api/v1/devices`

| Functionality       | Http Request                          | Http Status Code     |
|---------------------|---------------------------------------|----------------------|
| Create              | `POST /api/v1/devices`                | `201` and `Location` |
| Full/Partial Update | `PATCH /api/v1/devices/{id}`          | `200`                |
| Fetch Single        | `GET /api/v1/devices/{id}`            | `200`                |
| Fetch By Brand      | `GET /api/v1/devices?brand=Apple`     | `200`                |
| Fetch By State      | `GET /api/v1/devices?state=AVAILABLE` | `200`                |
| Fetch All           | `GET /api/v1/devices`                 | `200`                |
| Delete              | `DELETE /api/v1/devices/{id}`         | `204`                |


#### Curl Commands

```shell
    # Create - State is optional and defaults to AVAILABLE
    curl -i -X POST http://localhost:8080/api/v1/devices \
     -H "Content-Type: application/json" \
      -d "{\"name\":\"Galaxy 26\",\"brand\":\"Samsung\"}"
      
    # Update - Ignored properties are left unchanged  
    # Supports both full and partial
    
    # Partial update: 
    curl -i -X PATCH http://localhost:8080/api/v1/devices/1 \
    -H "Content-Type: application/json"  \
     -d "{\"name\":\"Pixel 10\"}" 
     
    # Full Update
    curl -i -X PATCH http://localhost:8080/api/v1/devices/1 \ 
    -H "Content-Type: application/json" 
    \ -d "{\"name\":\"Pixel 10\",\"brand\":\"Google\",\"state\":\"AVAILABLE\"}"
     
    # Fetch Single Device
    curl http://localhost:8080/api/v1/devices/1    
    
    # Fetch with filters
    curl http://localhost:8080/api/v1/devices?brand=google&state=IN_USE
     # Fetch All
    curl http://localhost:8080/api/v1/devices
    
    # Delete Single Device
    curl -i -X DELETE http://localhost:8080/api/v1/devices/3
    
```

| Code | Meaning                                  |
|------|------------------------------------------|
| 201  | Created                                  |
| 200  | Success                                  |
| 204  | Deleted                                  |
| 400  | Validation failure or invalid enum value |
| 404  | No Device with the id                    |
| 409  | Failed due to domain validation          |


```
{
    "detail":"Device 1 is in use, can not be updated",
    "instance":"/api/v1/devices/1",
    "status":409,
    "title":"Device is in use",
    "deviceId":1
}

{
    "detail":"Device 1 is in use, can not be deleted",
    "instance":"/api/v1/devices/1",
    "status":409,
    "title":"Device is in use",
    "deviceId":1
}

{
    "detail":"Device 123 not found",
    "instance":"/api/v1/devices/123",
    "status":404,
    "title":"Device Not Found"
}

{
    "detail":"No enum constant org.device.management.entity.DeviceState.USE",
    "instance":"/api/v1/devices",
    "status":400,
    "title":"Invalid Request"
}
```

## Implementation

* All seven functionalities along with domain validations are implemented. 
* Domain rules live in entity rather than service.
* Updating brand, name and delete operations are not allowed if device is in use
* Name and brand values are ordered first to avoid updating state change of in use device and then update.
* Device state should be updated first if it is in use then update remaining values.
* Following traditional project structure.
* Index is created on brand column.
* Database changes are transactional.

### Known Gaps and Future Improvements
* **Security**: Authentication and authorization can be added when the API requires authenticated access and
role-based permissions.
* **Pagination**: Pagination can be introduced as the device dataset grows to avoid returning large result 
sets in a single response.
* API Documentation: OpenAPI/Swagger documentation can be added to make the API contract easier to explore and consume.


# Unit Testing

The project contains unit and web-layer tests covering:

* Application context loading
* Device creation
* Device update
* Device retrieval
* Filtering by brand
* Filtering by state
* Device deletion
* Request validation
* Not-found handling
* Domain rules for IN_USE devices

Controller tests use @WebMvcTest and mock the service layer. 
Service/domain tests cover business rules independently of the HTTP layer.



