package org.acme.security;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import java.util.List;
import java.util.regex.Pattern;

@Provider
@Priority(Priorities.AUTHENTICATION - 100) // SECURITY: Execute early to prevent DoS via expensive auth hashing
public class MaxPageSizeFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(MaxPageSizeFilter.class);
    private static final int MAX_SIZE = 100;

    // ⚡ Bolt: Pre-compiled Pattern for sanitization to reduce CPU overhead
    private static final Pattern NEWLINE_PATTERN = Pattern.compile("[\r\n]");

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (validateParameter(requestContext, "size", 1, MAX_SIZE, "Page size must be between 1 and " + MAX_SIZE)) {
            return; // Aborted
        }
        if (validateParameter(requestContext, "page", 0, 10000, "Page index must be between 0 and 10000")) {
            return; // Aborted
        }
    }

    private boolean validateParameter(ContainerRequestContext requestContext, String paramName, int min, int max, String boundsErrorMessage) {
        List<String> params = requestContext.getUriInfo().getQueryParameters().get(paramName);
        if (params != null) {
            for (String param : params) {
                if (param != null) {
                    try {
                        int value = Integer.parseInt(param);
                        if (value < min || value > max) {
                            // SECURITY: Prevent DoS by bounding parameters
                            // SECURITY: Log the blocked request to enable security auditing. Prevent log injection by sanitizing the parameter.
                            LOG.warn("Blocked request with invalid " + paramName + " parameter: " + NEWLINE_PATTERN.matcher(param).replaceAll(""));
                            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
                                    .entity("{\"error\": \"" + boundsErrorMessage + "\"}")
                                    .type("application/json")
                                    .build());
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        // SECURITY: Log the blocked request to enable security auditing. Prevent log injection by sanitizing the parameter.
                        LOG.warn("Blocked request with non-numeric " + paramName + " parameter: " + NEWLINE_PATTERN.matcher(param).replaceAll(""));
                        requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
                                .entity("{\"error\": \"Invalid " + paramName + " parameter\"}")
                                .type("application/json")
                                .build());
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
