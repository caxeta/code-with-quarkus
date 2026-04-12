package org.acme.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    private static final Logger LOG = Logger.getLogger(WebApplicationExceptionMapper.class);

    @Override
    public Response toResponse(WebApplicationException exception) {
        // SECURITY: Preserve standard HTTP error semantics and headers (e.g. 'Allow' for 405 Method Not Allowed)
        // while sanitizing the response body to avoid leaking internal implementation details.

        // Use Response.fromResponse to preserve original headers from the exception
        Response originalResponse = exception.getResponse();

        // Get the status to decide on the generic message
        int status = originalResponse.getStatus();
        String message = "An error occurred";
        if (status == 404) {
             message = "Resource not found";
        } else if (status == 405) {
             message = "Method not allowed";
        } else if (status >= 400 && status < 500) {
             message = "Bad request";
        }

        return Response.fromResponse(originalResponse)
                .entity("{\"error\": \"" + message + "\"}")
        // SECURITY: Ensure WebApplicationExceptions (like 405 Not Allowed, 401 Unauthorized)
        // are not swallowed by GlobalExceptionMapper. Use Response.fromResponse to preserve
        // critical original HTTP headers (e.g. 'Allow' or 'WWW-Authenticate').
        Response response = exception.getResponse();
        String errorMsg = response.getStatusInfo() != null ? response.getStatusInfo().getReasonPhrase() : "HTTP Error";

        return Response.fromResponse(response)
                .entity("{\"error\": \"" + errorMsg + "\"}")
                .type("application/json")
                .build();
    }
}
