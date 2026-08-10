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
        // SECURITY: Log the error for auditing, sanitizing any user input to prevent Log Injection
        // Only log server errors (5xx) to prevent DoS via log spamming for client errors (4xx)
        Response response = exception.getResponse();

        if (response != null && response.getStatus() >= 500) {
            String sanitizedMsg = exception.getMessage() != null ? NEWLINE_PATTERN.matcher(exception.getMessage()).replaceAll("") : "null";
            LOG.warn("WebApplicationException caught: " + sanitizedMsg);
        }

        // SECURITY: Ensure WebApplicationExceptions (like 405 Not Allowed, 401 Unauthorized)
        // are not swallowed by GlobalExceptionMapper. Use Response.fromResponse to preserve
        // critical original HTTP headers (e.g. 'Allow' or 'WWW-Authenticate').
        String errorMsg = response.getStatusInfo() != null ? response.getStatusInfo().getReasonPhrase() : "HTTP Error";

        return Response.fromResponse(response)
                .entity("{\"error\": \"" + errorMsg + "\"}")
                .type("application/json")
                .build();
    }
}
