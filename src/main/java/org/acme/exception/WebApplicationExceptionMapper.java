package org.acme.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import java.util.regex.Pattern;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    private static final Logger LOG = Logger.getLogger(WebApplicationExceptionMapper.class);
    private static final Pattern NEWLINE_PATTERN = Pattern.compile("[\r\n]");

    @Override
    public Response toResponse(WebApplicationException exception) {
        // SECURITY: Ensure WebApplicationExceptions (like 405 Not Allowed, 401 Unauthorized)
        // are not swallowed by GlobalExceptionMapper. Use Response.fromResponse to preserve
        // critical original HTTP headers (e.g. 'Allow' or 'WWW-Authenticate').
        Response response = exception.getResponse();
        String errorMsg = response.getStatusInfo() != null ? response.getStatusInfo().getReasonPhrase() : "HTTP Error";

        // SECURITY: Audit log the security exception. Prevent Log Injection by stripping newlines.
        String sanitizedMessage = exception.getMessage() != null ? NEWLINE_PATTERN.matcher(exception.getMessage()).replaceAll("") : "null";

        if (response.getStatus() >= 500) {
            LOG.error("Server error WebApplicationException (" + response.getStatus() + "): " + sanitizedMessage, exception);
        } else {
            LOG.warn("Client error WebApplicationException (" + response.getStatus() + "): " + sanitizedMessage);
        }

        return Response.fromResponse(response)
                .entity("{\"error\": \"" + errorMsg + "\"}")
                .type("application/json")
                .build();
    }
}
