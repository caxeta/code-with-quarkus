package org.acme;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CacheControlFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        // SECURITY: Add Cache-Control headers to API responses to prevent browser caching of potentially sensitive data.
        // We only add it if a Cache-Control header has not already been explicitly set by the endpoint.
        if (responseContext.getHeaderString("Cache-Control") == null) {
            responseContext.getHeaders().add("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            responseContext.getHeaders().add("Pragma", "no-cache");
            responseContext.getHeaders().add("Expires", "0");
        }
    }
}
