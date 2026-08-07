package org.acme.exception;

import io.quarkus.rest.data.panache.RestDataPanacheException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import java.util.regex.Pattern;

@Provider
public class RestDataPanacheExceptionMapper implements ExceptionMapper<RestDataPanacheException> {

    private static final Logger LOG = Logger.getLogger(RestDataPanacheExceptionMapper.class);

    // ⚡ Bolt: Pre-compiled Pattern for sanitization to reduce CPU overhead
    private static final Pattern NEWLINE_PATTERN = Pattern.compile("[\r\n]");

    @Override
    public Response toResponse(RestDataPanacheException exception) {
        // SECURITY: Do not leak stack traces or exception messages which might contain query structure
        // SECURITY: Prevent Log Injection by stripping newlines from the exception message, which may contain user input
        String sanitizedMessage = exception.getMessage() != null ? NEWLINE_PATTERN.matcher(exception.getMessage()).replaceAll("") : "null";
        LOG.warn("Caught RestDataPanacheException: " + sanitizedMessage);
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"Invalid request parameters\"}")
                .type("application/json")
                .build();
    }
}
