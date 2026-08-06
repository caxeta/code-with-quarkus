package org.acme.exception;

import io.quarkus.rest.data.panache.RestDataPanacheException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RestDataPanacheExceptionMapperTest {

    private RestDataPanacheExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RestDataPanacheExceptionMapper();
    }

    @Test
    void testToResponse_NormalMessage() {
        RestDataPanacheException exception = new RestDataPanacheException("Invalid request", new RuntimeException());
        Response response = mapper.toResponse(exception);

        assertNotNull(response);
        assertEquals(400, response.getStatus());
        assertEquals("{\"error\": \"Invalid request parameters\"}", response.getEntity());
        assertEquals("application/json", response.getMediaType().toString());
    }

    @Test
    void testToResponse_MessageWithNewlines() {
        RestDataPanacheException exception = new RestDataPanacheException("Invalid\nrequest\r", new RuntimeException());
        Response response = mapper.toResponse(exception);

        assertNotNull(response);
        assertEquals(400, response.getStatus());
        assertEquals("{\"error\": \"Invalid request parameters\"}", response.getEntity());
    }

    @Test
    void testToResponse_NullMessage() {
        RestDataPanacheException exception = new RestDataPanacheException((String) null, new RuntimeException());
        Response response = mapper.toResponse(exception);

        assertNotNull(response);
        assertEquals(400, response.getStatus());
        assertEquals("{\"error\": \"Invalid request parameters\"}", response.getEntity());
    }
}