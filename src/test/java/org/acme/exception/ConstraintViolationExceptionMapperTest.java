package org.acme.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConstraintViolationExceptionMapperTest {

    @Test
    public void testToResponse() {
        ConstraintViolationExceptionMapper mapper = new ConstraintViolationExceptionMapper();
        ConstraintViolationException exception = new ConstraintViolationException("Validation error", Collections.emptySet());

        Response response = mapper.toResponse(exception);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("{\"error\": \"Validation failed\"}", response.getEntity());
        assertEquals("application/json", response.getMediaType().toString());
    }
}
