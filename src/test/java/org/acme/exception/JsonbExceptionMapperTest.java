package org.acme.exception;

import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JsonbExceptionMapperTest {

    @Test
    public void testToResponse() {
        JsonbExceptionMapper mapper = new JsonbExceptionMapper();
        JsonbException exception = new JsonbException("test exception");

        Response response = mapper.toResponse(exception);

        assertNotNull(response);
        assertEquals(400, response.getStatus());
        assertEquals("{\"error\": \"Invalid JSON payload\"}", response.getEntity());
        assertNotNull(response.getMediaType());
        assertEquals("application/json", response.getMediaType().toString());
    }
}
