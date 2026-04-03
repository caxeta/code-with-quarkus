package org.acme.exception;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Override
    public Response toResponse(NotFoundException exception) {
        // SECURITY: Prevent GlobalExceptionMapper from intercepting framework NotFoundExceptions,
        // which could lead to returning generic 500s and filling logs with stack traces.
        return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"Resource not found\"}")
                .type("application/json")
                .build();
    }
}
