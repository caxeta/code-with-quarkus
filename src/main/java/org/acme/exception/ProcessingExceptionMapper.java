package org.acme.exception;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ProcessingExceptionMapper implements ExceptionMapper<ProcessingException> {

    @Override
    public Response toResponse(ProcessingException exception) {
        // SECURITY: Don't leak stack traces or internal implementation details
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"Malformed payload\"}")
                .type("application/json")
                .build();
    }
}
