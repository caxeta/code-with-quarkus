package org.acme.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException exception) {
        // SECURITY: Ensure WebApplicationExceptions (like 405 Not Allowed, 401 Unauthorized)
        // are not swallowed by GlobalExceptionMapper. Use Response.fromResponse to preserve
        // critical original HTTP headers (e.g. 'Allow' or 'WWW-Authenticate').
        String error = exception.getResponse().getStatusInfo().getReasonPhrase();
        return Response.fromResponse(exception.getResponse())
                .entity("{\"error\": \"" + error + "\"}")
                .type("application/json")
                .build();
    }
}
