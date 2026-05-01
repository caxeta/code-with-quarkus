package org.acme.security;

import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Provider
@Priority(Priorities.AUTHENTICATION - 100) // SECURITY: Execute rate limiting BEFORE authentication to prevent DoS via expensive auth hashing
public class RateLimitFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(RateLimitFilter.class);

    private static final int MAX_REQUESTS = 100;
    private final ConcurrentHashMap<String, AtomicInteger> counts = new ConcurrentHashMap<>();
    private volatile long currentWindow = System.currentTimeMillis() / 60000;

    @Context
    HttpServerRequest request;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        long window = System.currentTimeMillis() / 60000;
        if (window > currentWindow) {
            synchronized(this) {
                if (window > currentWindow) {
                    counts.clear();
                    currentWindow = window;
                }
            }
        }

        String clientIp = request.remoteAddress().host();
        int count = counts.computeIfAbsent(clientIp, k -> new AtomicInteger(0)).incrementAndGet();

        if (count > MAX_REQUESTS) {
            // SECURITY: Log abusive IPs for auditing
            LOG.warn("Rate limit exceeded for IP: " + clientIp);

            // SECURITY: Mitigate DoS and brute-force attacks by rate-limiting requests per IP
            requestContext.abortWith(Response.status(429)
                    .header("Retry-After", "60")
                    .entity("{\"error\": \"Too Many Requests\"}")
                    .type("application/json")
                    .build());
        }
    }
}
