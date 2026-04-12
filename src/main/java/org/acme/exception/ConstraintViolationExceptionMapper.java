package org.acme.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        // SECURITY: Don't leak internal implementation details (like method paths) or echo malicious/sensitive input in validation responses
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"Validation failed\"}")
                .type("application/json")
                .build();
    }
}
