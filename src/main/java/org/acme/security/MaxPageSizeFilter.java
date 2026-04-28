package org.acme.security;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION - 100) // SECURITY: Execute early to prevent DoS via expensive auth hashing
public class MaxPageSizeFilter implements ContainerRequestFilter {

    private static final int MAX_SIZE = 100;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String sizeParam = requestContext.getUriInfo().getQueryParameters().getFirst("size");
        if (sizeParam != null) {
            try {
                int size = Integer.parseInt(sizeParam);
                if (size <= 0 || size > MAX_SIZE) {
                    // SECURITY: Prevent DoS via resource exhaustion and invalid offsets by bounding the page size
                    requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"error\": \"Page size must be between 1 and " + MAX_SIZE + "\"}")
                            .type("application/json")
                            .build());
                    return;
                }
            } catch (NumberFormatException e) {
                requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Invalid size parameter\"}")
                        .type("application/json")
                        .build());
                return;
            }
        }

        String pageParam = requestContext.getUriInfo().getQueryParameters().getFirst("page");
        if (pageParam != null) {
            try {
                int page = Integer.parseInt(pageParam);
                if (page < 0 || page > 10000) {
                    // SECURITY: Prevent DoS via deep pagination and limit database impact
                    requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"error\": \"Page index must be between 0 and 10000\"}")
                            .type("application/json")
                            .build());
                }
            } catch (NumberFormatException e) {
                requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Invalid page parameter\"}")
                        .type("application/json")
                        .build());
            }
        }
    }
}
