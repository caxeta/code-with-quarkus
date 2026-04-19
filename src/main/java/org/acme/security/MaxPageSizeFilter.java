package org.acme.security;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
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
                }
            } catch (NumberFormatException e) {
                requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Invalid size parameter\"}")
                        .type("application/json")
                        .build());
            }
        }
    }
}
