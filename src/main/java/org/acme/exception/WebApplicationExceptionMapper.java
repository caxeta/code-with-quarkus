package org.acme.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException exception) {
        Response response = exception.getResponse();
        // SECURITY: Return the standard HTTP status and generic phrase, preventing fallback to GlobalExceptionMapper
        // which would leak internal server errors and false-positive stack traces
        if (response.hasEntity()) {
             return response;
        }

        return Response.fromResponse(response)
                .entity("{\"error\": \"" + response.getStatusInfo().getReasonPhrase() + "\"}")
                .type("application/json")
                .build();
    }
}
