package org.acme.exception;

import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JsonbExceptionMapper implements ExceptionMapper<JsonbException> {

    @Override
    public Response toResponse(JsonbException exception) {
        // SECURITY: Don't leak stack traces or internal implementation details
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"Invalid JSON payload\"}")
                .type("application/json")
                .build();
    }
}
