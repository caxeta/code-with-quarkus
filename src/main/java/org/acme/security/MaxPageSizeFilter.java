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
        List<String> sizeParams = requestContext.getUriInfo().getQueryParameters().get("size");
        if (sizeParams != null) {
            for (String sizeParam : sizeParams) {
                if (sizeParam != null) {
                    try {
                        int size = Integer.parseInt(sizeParam);
                        if (size <= 0 || size > MAX_SIZE) {
                            // SECURITY: Prevent DoS via resource exhaustion and invalid offsets by bounding the page size
                            // SECURITY: Log the blocked request to enable security auditing. Prevent log injection by sanitizing the parameter.
                            LOG.warn("Blocked request with invalid size parameter: " + sizeParam.replaceAll("[\r\n]", ""));
                            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
                                    .entity("{\"error\": \"Page size must be between 1 and " + MAX_SIZE + "\"}")
                                    .type("application/json")
                                    .build());
                            return;
                        }
                    } catch (NumberFormatException e) {
                        // SECURITY: Log the blocked request to enable security auditing. Prevent log injection by sanitizing the parameter.
                        LOG.warn("Blocked request with non-numeric size parameter: " + sizeParam.replaceAll("[\r\n]", ""));
                        requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
                                .entity("{\"error\": \"Invalid size parameter\"}")
                                .type("application/json")
                                .build());
                        return;
                    }
                }
            }
        }

        List<String> pageParams = requestContext.getUriInfo().getQueryParameters().get("page");
        if (pageParams != null) {
            for (String pageParam : pageParams) {
                if (pageParam != null) {
                    try {
                        int page = Integer.parseInt(pageParam);
                        if (page < 0 || page > 10000) {
                            // SECURITY: Prevent DoS via deep pagination and limit database impact
                            // SECURITY: Log the blocked request to enable security auditing. Prevent log injection by sanitizing the parameter.
                            LOG.warn("Blocked request with invalid page parameter: " + pageParam.replaceAll("[\r\n]", ""));
                            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
                                    .entity("{\"error\": \"Page index must be between 0 and 10000\"}")
                                    .type("application/json")
                                    .build());
                            return;
                        }
                    } catch (NumberFormatException e) {
                        // SECURITY: Log the blocked request to enable security auditing. Prevent log injection by sanitizing the parameter.
                        LOG.warn("Blocked request with non-numeric page parameter: " + pageParam.replaceAll("[\r\n]", ""));
                        requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
                                .entity("{\"error\": \"Invalid page parameter\"}")
                                .type("application/json")
                                .build());
                        return;
                    }
                }
            }
        }
    }
}
