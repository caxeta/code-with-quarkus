package org.acme.security;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import java.util.List;

@Provider
@Priority(Priorities.AUTHENTICATION - 100) // SECURITY: Execute early to prevent DoS via expensive auth hashing
public class MaxPageSizeFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(MaxPageSizeFilter.class);
    private static final int MAX_SIZE = 100;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // SECURITY: Prevent DoS via resource exhaustion and invalid offsets by bounding the page size
        if (!validateParameter(requestContext, "size", 1, MAX_SIZE, "Page size must be between 1 and " + MAX_SIZE)) {
            return;
        }

        // SECURITY: Prevent DoS via deep pagination and limit database impact
        if (!validateParameter(requestContext, "page", 0, 10000, "Page index must be between 0 and 10000")) {
            return;
        }
    }

    private boolean validateParameter(ContainerRequestContext requestContext, String paramName, int min, int max, String errorMessage) {
        List<String> params = requestContext.getUriInfo().getQueryParameters().get(paramName);
        if (params != null) {
            for (String param : params) {
                if (param != null) {
                    try {
                        int value = Integer.parseInt(param);
                        if (value < min || value > max) {
                            // SECURITY: Log the blocked request to enable security auditing. Prevent log injection by sanitizing the parameter.
                            LOG.warn("Blocked request with invalid " + paramName + " parameter: " + param.replaceAll("[\r\n]", ""));
                            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
                                    .entity("{\"error\": \"" + errorMessage + "\"}")
                                    .type("application/json")
                                    .build());
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        // SECURITY: Log the blocked request to enable security auditing. Prevent log injection by sanitizing the parameter.
                        LOG.warn("Blocked request with non-numeric " + paramName + " parameter: " + param.replaceAll("[\r\n]", ""));
                        requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
                                .entity("{\"error\": \"Invalid " + paramName + " parameter\"}")
                                .type("application/json")
                                .build());
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
