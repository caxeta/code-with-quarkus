package org.acme;

import io.quarkus.rest.data.panache.RestDataPanacheException;
import jakarta.ws.rs.core.Response;
import org.acme.exception.RestDataPanacheExceptionMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RestDataPanacheExceptionMapperTest {

    @Test
    public void testToResponseWithNormalMessage() {
        RestDataPanacheExceptionMapper mapper = new RestDataPanacheExceptionMapper();
        RestDataPanacheException exception = new RestDataPanacheException("Invalid sort field", new RuntimeException());

        Response response = mapper.toResponse(exception);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("{\"error\": \"Invalid request parameters\"}", response.getEntity());
        assertEquals("application/json", response.getMediaType().toString());
    }

    @Test
    public void testToResponseWithMessageContainingNewlines() {
        RestDataPanacheExceptionMapper mapper = new RestDataPanacheExceptionMapper();
        RestDataPanacheException exception = new RestDataPanacheException("Invalid\n\r sort field", new RuntimeException());

        Response response = mapper.toResponse(exception);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("{\"error\": \"Invalid request parameters\"}", response.getEntity());
    }

    @Test
    public void testToResponseWithNullMessage() {
        RestDataPanacheExceptionMapper mapper = new RestDataPanacheExceptionMapper();
        RestDataPanacheException exception = new RestDataPanacheException((String) null, new RuntimeException());

        Response response = mapper.toResponse(exception);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("{\"error\": \"Invalid request parameters\"}", response.getEntity());
    }
}